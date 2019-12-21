package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.createPublishRequest
import com.gilbertojrequena.memsns.core.manager.PublicationManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class Publish(private val publicationManager: PublicationManager) :
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