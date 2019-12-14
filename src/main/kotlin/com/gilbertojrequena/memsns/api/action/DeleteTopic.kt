package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.validateAndGet
import com.gilbertojrequena.memsns.core.exception.TopicNotFoundException
import com.gilbertojrequena.memsns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class DeleteTopic(private val topicManager: TopicManager) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        try {
            topicManager.delete(params.validateAndGet("TopicArn"))
        } catch (e: TopicNotFoundException) {
        }

        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("DeleteTopicResponse") {
                    awsMetadata()
                })
        }
    }
}