package io.github.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.bonsai_sns.api.awsMetadata
import com.gilbertojrequena.bonsai_sns.api.createTopicData
import com.gilbertojrequena.bonsai_sns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class CreateTopic(private val topicManager: TopicManager) :
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

