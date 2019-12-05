package com.gilbertojrequena.memsns.core.actor

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.dispatcher.HttpMessageDispatcher
import com.gilbertojrequena.memsns.core.actor.dispatcher.SqsMessageDispatcher
import com.gilbertojrequena.memsns.core.actor.message.PublishMessage
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch

fun publishActor(snsOpsActor: SendChannel<SnsOpsMessage>) = GlobalScope.actor<PublishMessage> {
    with(MessageDispatchManager(snsOpsActor, SnsHttpClient())) {
        for (message in channel) {
            when (message) {
                is PublishMessage.Publish -> dispatchMessages(
                    message.publishRequest
                )
            }
        }
    }
}

private class MessageDispatchManager(
    private val snsOpsActor: SendChannel<SnsOpsMessage>,
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
        val subsAndToken = getSubscriptions(publishRequest)
        for (subscription in subsAndToken.subscriptions) {
            publishScope.launch {
                val responseChannel = Channel<Topic?>()
                snsOpsActor.send(SnsOpsMessage.FindTopicByArn(subscription.topicArn, responseChannel))
                val topic = responseChannel.receive() ?: throw TODO()
                val messageDispatcher = dispatcherByProtocol[subscription.protocol] ?: throw TODO()
                messageDispatcher.publish(topic, subscription, publishRequest.message, publishRequest.messageId)
            }
        }
    }

    private suspend fun getSubscriptions(publishRequest: PublishRequest): SubscriptionsAndToken {
        val responseChannel = Channel<SubscriptionsAndToken>()
        snsOpsActor.send(SnsOpsMessage.FindAllSubscriptionsByTopic(publishRequest.topicArn, null, responseChannel))
        return responseChannel.receive() //TODO use the pagination
    }
}