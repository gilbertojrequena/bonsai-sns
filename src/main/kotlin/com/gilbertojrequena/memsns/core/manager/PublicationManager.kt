package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.PublishRequest
import com.gilbertojrequena.memsns.core.SubscriptionsAndToken
import com.gilbertojrequena.memsns.core.actor.message.PublishMessage
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import java.util.*

class PublicationManager(
    private val snsOpsActor: SendChannel<SnsOpsMessage>,
    private val snsPublicationActor: SendChannel<PublishMessage>
) {

    suspend fun publish(publishRequest: PublishRequest): String {
        val responseChannel = Channel<SubscriptionsAndToken>()
        snsOpsActor.send(SnsOpsMessage.FindAllSubscriptionsByTopic(publishRequest.topicArn, null, responseChannel))
        val subsAndToken = responseChannel.receive() //TODO this could be more than 100 or 0

        val messageId = UUID.randomUUID().toString()

        for (sub in subsAndToken.subscriptions) {
            snsPublicationActor.send(PublishMessage.Publish(sub, publishRequest.message, messageId))
        }
        return messageId
    }
}