package com.gilbertojrequena.bonsai_sns.core.actor

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.gilbertojrequena.bonsai_sns.core.*
import com.gilbertojrequena.bonsai_sns.core.actor.dispatcher.HttpMessageDispatcher
import com.gilbertojrequena.bonsai_sns.core.actor.dispatcher.MessageDispatcher
import com.gilbertojrequena.bonsai_sns.core.actor.dispatcher.MessageFactory
import com.gilbertojrequena.bonsai_sns.core.actor.dispatcher.SqsMessageDispatcher
import com.gilbertojrequena.bonsai_sns.core.actor.message.PublishMessage
import com.gilbertojrequena.bonsai_sns.core.exception.MessageDispatchException
import com.gilbertojrequena.bonsai_sns.core.exception.MessageDispatcherNotFoundException
import com.gilbertojrequena.bonsai_sns.core.filter_policy.FilterPolicyEvaluator
import com.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import com.gilbertojrequena.bonsai_sns.server.BonsaiSnsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import mu.KotlinLogging

internal fun publishActor(subscriptionManager: SubscriptionManager, config: BonsaiSnsConfig) =
    GlobalScope.actor<PublishMessage> {
        with(MessageDispatchManager(subscriptionManager, MessageFactory(config), RetriableHttpClient(), config)) {
            for (message in channel) {
                when (message) {
                    is PublishMessage.Publish -> dispatchMessages(message.publishRequest)
                }
            }
        }
    }

private class MessageDispatchManager(
    private val subscriptionManager: SubscriptionManager,
    private val messageFactory: MessageFactory,
    httpClient: RetriableHttpClient,
    config: BonsaiSnsConfig
) {

    private val log = KotlinLogging.logger {}
    private val publishScope = CoroutineScope(Dispatchers.IO)
    private val filterPolicyEvaluator = FilterPolicyEvaluator()
    private var sqsMessageDispatcher: MessageDispatcher
    private var httpMessageDispatcher: MessageDispatcher
    private var dispatcherByProtocol: Map<Subscription.Protocol, MessageDispatcher>

    init {
        val sqsClient = if (!config.sqsEndpoint.isNullOrBlank()) {
            AmazonSQSClientBuilder.standard()
                .withCredentials(
                    AWSStaticCredentialsProvider(BasicAWSCredentials(config.sqsAccessKey, config.sqsSecretKey))
                )
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(config.sqsEndpoint, config.region))
                .build()
        } else null
        httpMessageDispatcher = HttpMessageDispatcher(httpClient)
        sqsMessageDispatcher = SqsMessageDispatcher(sqsClient)

        dispatcherByProtocol = mapOf(
            Subscription.Protocol.HTTP to httpMessageDispatcher,
            Subscription.Protocol.HTTPS to httpMessageDispatcher,
            Subscription.Protocol.SQS to sqsMessageDispatcher
        )
    }

    suspend fun dispatchMessages(publishRequest: PublishRequest) {
        do {
            val subsAndToken = subscriptionManager.findAllByTopicArn(publishRequest.topicArn)
            for (subscription in subsAndToken.subscriptions) {
                val attributes = subscriptionManager.findSubscriptionAttributes(subscription.arn)
                publishScope.launch {
                    val messageDispatcher =
                        dispatcherByProtocol[subscription.protocol] ?: throw MessageDispatcherNotFoundException(
                            subscription.protocol
                        )
                    val subscriptionPolicy = attributes["FilterPolicy"]
                    if (subscriptionPolicy.isNullOrBlank() || messageIsAccepted(publishRequest, subscriptionPolicy)) {
                        val message = messageFactory.create(
                            publishRequest.message.body,
                            SubscriptionWithAttributes(subscription, attributes),
                            publishRequest.messageId
                        )
                        try {
                            messageDispatcher.dispatch(subscription.endpoint, message)
                        } catch (e: MessageDispatchException) {
                            val redrivePolicy = attributes["RedrivePolicy"]
                            if (!redrivePolicy.isNullOrBlank()) {
                                val dlqArn = JsonMapper.instance().read(redrivePolicy)
                                    .get("deadLetterTargetArn").textValue()
                                try {
                                    log.debug {
                                        "Sending message to dead letter queue, endpoint: '${subscription.endpoint}', " +
                                                "message: '$message', dlq arn: '$dlqArn'"
                                    }
                                    sqsMessageDispatcher.dispatch(dlqArn, message)
                                } catch (e: MessageDispatchException) {
                                    log.debug {
                                        "Message could not be delivered to dead letter queue, " +
                                                "endpoint: '${subscription.endpoint}', message: '$message'" +
                                                ", dlq arn: '$dlqArn'"
                                    }
                                }
                            } else {
                                log.debug {
                                    "Message will be lost, there is no dlq configured for subscription, " +
                                            "endpoint: ${subscription.endpoint}, message: '$message', subscription: $subscription"
                                }
                            }
                        }
                    }
                }
            }
        } while (!subsAndToken.nextToken.isNullOrBlank())
    }

    private fun messageIsAccepted(publishRequest: PublishRequest, policy: String): Boolean {
        log.debug { "Should ${publishRequest.message} be accepted with policy: '$policy' ?" }
        if (publishRequest.message.attributes.isEmpty()) {
            log.info { "Rejecting ${publishRequest.message} because of empty attributes" }
            return false
        }
        return filterPolicyEvaluator.eval(publishRequest.message.attributes, policy)
    }
}