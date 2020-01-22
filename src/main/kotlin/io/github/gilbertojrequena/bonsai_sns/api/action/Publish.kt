package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.createPublishRequest
import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.manager.PublicationManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging

internal class Publish(private val publicationManager: PublicationManager) :
    Action {
    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val messageId = publicationManager.publish(params.createPublishRequest())

        log.debug { "Message $messageId ready to be published" }
        call.respondText {
            xml("PublishResponse") {
                element("PublishResult") {
                    element("MessageId") {
                        text = messageId
                    }
                }
            }
        }
    }
}