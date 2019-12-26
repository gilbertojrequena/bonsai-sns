package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.PublishRequest
import com.gilbertojrequena.memsns.core.actor.message.PublishMessage
import kotlinx.coroutines.channels.SendChannel
import java.util.*

class PublicationManager(
    private val snsPublicationActor: SendChannel<PublishMessage>
) {

    suspend fun publish(publishRequest: PublishRequest): String {
        val messageId = UUID.randomUUID().toString()
        snsPublicationActor.send(
            PublishMessage.Publish(
                PublishRequest(
                    publishRequest.topicArn,
                    publishRequest.message,
                    messageId
                )
            )
        )
        return messageId
    }
}