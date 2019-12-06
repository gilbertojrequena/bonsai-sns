package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class DeleteTopic(private val topicManager: TopicManager) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {

        topicManager.delete(params["TopicArn"] ?: throw TODO())

        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("DeleteTopicResponse") {
                    awsMetadata()
                })
        }
    }
}