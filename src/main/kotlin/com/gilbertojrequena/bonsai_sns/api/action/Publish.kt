package com.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.bonsai_sns.api.awsMetadata
import com.gilbertojrequena.bonsai_sns.api.createPublishRequest
import com.gilbertojrequena.bonsai_sns.core.manager.PublicationManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class Publish(private val publicationManager: PublicationManager) :
    Action {
    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val messageId = publicationManager.publish(params.createPublishRequest())

        log.debug { "Message $messageId ready to be published" }
        call.respondText {
            writeXmlElement(jdom("PublishResponse") {
                element("PublishResult") {
                    element("MessageId") {
                        text(messageId)
                    }
                }
                awsMetadata()

            })
        }
    }
}