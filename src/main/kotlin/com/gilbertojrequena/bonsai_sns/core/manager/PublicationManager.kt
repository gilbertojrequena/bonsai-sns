package com.gilbertojrequena.bonsai_sns.core.manager

import com.gilbertojrequena.bonsai_sns.core.PublishRequest
import com.gilbertojrequena.bonsai_sns.core.actor.message.PublishMessage
import kotlinx.coroutines.channels.SendChannel
import java.util.*

internal class PublicationManager(
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