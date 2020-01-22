package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.validateAndGet
import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.exception.TopicNotFoundException
import io.github.gilbertojrequena.bonsai_sns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging

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
            xml("DeleteTopicResponse")
        }
    }
}