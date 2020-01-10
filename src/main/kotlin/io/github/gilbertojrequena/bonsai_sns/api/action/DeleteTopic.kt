package io.github.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.ObjectMapper
import com.gilbertojrequena.bonsai_sns.api.awsMetadata
import com.gilbertojrequena.bonsai_sns.api.validateAndGet
import com.gilbertojrequena.bonsai_sns.core.exception.TopicNotFoundException
import com.gilbertojrequena.bonsai_sns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class DeleteTopic(private val topicManager: TopicManager) : Action {
    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val arn = params.validateAndGet("TopicArn")
        try {
            topicManager.delete(arn)
        } catch (e: TopicNotFoundException) {
        }
        log.info { "Topic with arn: $arn deleted" }

        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("DeleteTopicResponse") {
                    awsMetadata()
                })
        }
    }
}