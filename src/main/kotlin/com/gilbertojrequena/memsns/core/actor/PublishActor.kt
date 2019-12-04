package com.gilbertojrequena.memsns.core.actor

import com.gilbertojrequena.memsns.core.SnsHttpClient
import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.Topic
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
                is PublishMessage.Publish -> dispatchMessage(
                    message.subscription,
                    message.message,
                    message.messageId
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

    suspend fun dispatchMessage(
        subscription: Subscription,
        message: String,
        messageId: String
    ) {
        publishScope.launch {
            val responseChannel = Channel<Topic?>()
            snsOpsActor.send(SnsOpsMessage.FindTopicByArn(subscription.topicArn, responseChannel))
            val topic = responseChannel.receive() ?: throw TODO()
            val messageDispatcher = dispatcherByProtocol[subscription.protocol] ?: throw TODO()
            messageDispatcher.publish(topic, subscription, message, messageId)
        }
    }
}