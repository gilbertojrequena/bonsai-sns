package com.gilbertojrequena.memsns.core.actor

import com.gilbertojrequena.memsns.core.PublishRequest
import com.gilbertojrequena.memsns.core.SnsHttpClient
import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.SubscriptionWithAttributes
import com.gilbertojrequena.memsns.core.actor.dispatcher.HttpMessageDispatcher
import com.gilbertojrequena.memsns.core.actor.dispatcher.MessageFactory
import com.gilbertojrequena.memsns.core.actor.dispatcher.SqsMessageDispatcher
import com.gilbertojrequena.memsns.core.actor.message.PublishMessage
import com.gilbertojrequena.memsns.core.exception.MessageDispatcherNotFoundException
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import com.gilbertojrequena.memsns.server.MemSnsConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch

fun publishActor(
    subscriptionManager: SubscriptionManager,
    config: MemSnsConfig
) = GlobalScope.actor<PublishMessage> {
    with(MessageDispatchManager(subscriptionManager, MessageFactory(config), SnsHttpClient())) {
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
    snsHttpClient: SnsHttpClient
) {

    private val publishScope = CoroutineScope(Dispatchers.IO)

    private val dispatcherByProtocol = mapOf(
        Subscription.Protocol.HTTP to HttpMessageDispatcher(snsHttpClient),
        Subscription.Protocol.HTTPS to HttpMessageDispatcher(snsHttpClient),
        Subscription.Protocol.SQS to SqsMessageDispatcher(snsHttpClient)
    )

    suspend fun dispatchMessages(
        publishRequest: PublishRequest
    ) {
        do {
            val subsAndToken = subscriptionManager.findAllByTopicArn(publishRequest.topicArn)
            for (subscription in subsAndToken.subscriptions) {
                val subscriptionAttributes = subscriptionManager.findSubscriptionAttributes(subscription.arn)
                publishScope.launch {
                    val messageDispatcher =
                        dispatcherByProtocol[subscription.protocol] ?: throw MessageDispatcherNotFoundException(
                            subscription.protocol
                        )

                    val message = messageFactory.create(
                        publishRequest.message,
                        SubscriptionWithAttributes(subscription, subscriptionAttributes),
                        publishRequest.messageId
                    )
                    messageDispatcher.dispatch(subscription.endpoint, message)
                }
            }
        } while (subsAndToken.nextToken != null)
    }
}