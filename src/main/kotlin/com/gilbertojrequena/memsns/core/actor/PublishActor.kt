package com.gilbertojrequena.memsns.core.actor

import com.gilbertojrequena.memsns.core.PublishRequest
import com.gilbertojrequena.memsns.core.RetriableHttpClient
import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.SubscriptionWithAttributes
import com.gilbertojrequena.memsns.core.actor.dispatcher.HttpMessageDispatcher
import com.gilbertojrequena.memsns.core.actor.dispatcher.MessageFactory
import com.gilbertojrequena.memsns.core.actor.dispatcher.SqsMessageDispatcher
import com.gilbertojrequena.memsns.core.actor.message.PublishMessage
import com.gilbertojrequena.memsns.core.exception.MessageDispatcherNotFoundException
import com.gilbertojrequena.memsns.core.filter_policy.FilterPolicyEvaluator
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import com.gilbertojrequena.memsns.server.MemSnsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import mu.KotlinLogging

internal fun publishActor(subscriptionManager: SubscriptionManager, config: MemSnsConfig) =
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
    config: MemSnsConfig
) {

    private val log = KotlinLogging.logger {}
    private val publishScope = CoroutineScope(Dispatchers.IO)
    private val filterPolicyEvaluator = FilterPolicyEvaluator()

    private val dispatcherByProtocol = mapOf(
        Subscription.Protocol.HTTP to HttpMessageDispatcher(httpClient),
        Subscription.Protocol.HTTPS to HttpMessageDispatcher(httpClient),
        Subscription.Protocol.SQS to SqsMessageDispatcher(config)
    )

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
                    if (subscriptionPolicy == null || messageIsAccepted(publishRequest, subscriptionPolicy)) {
                        val message = messageFactory.create(
                            publishRequest.message.body,
                            SubscriptionWithAttributes(subscription, attributes),
                            publishRequest.messageId
                        )
                        messageDispatcher.dispatch(subscription.endpoint, message)
                    }
                }
            }
        } while (subsAndToken.nextToken != null)
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