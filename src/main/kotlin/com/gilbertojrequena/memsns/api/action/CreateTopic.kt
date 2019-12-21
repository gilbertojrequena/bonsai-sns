package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.createTopicData
import com.gilbertojrequena.memsns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class CreateTopic(private val topicManager: TopicManager) :
    Action {
    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val topic = topicManager.create(params.createTopicData())

        log.info { "Topic $topic created" }
        call.respondText {
            writeXmlElement(
                jdom("CreateTopicResponse") {
                    element("CreateTopicResult") {
                        element("TopicArn") {
                            text(topic.arn)
                        }
                    }
                    awsMetadata()
                })
        }
    }
}

