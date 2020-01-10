package io.github.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.ObjectMapper
import com.gilbertojrequena.bonsai_sns.api.awsMetadata
import com.gilbertojrequena.bonsai_sns.api.validateAndGet
import com.gilbertojrequena.bonsai_sns.core.exception.SubscriptionNotFoundException
import com.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class Unsubscribe(private val subscriptionManager: SubscriptionManager) : Action {
    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val arn = params.validateAndGet("SubscriptionArn")
        try {
            subscriptionManager.delete(arn)
        } catch (e: SubscriptionNotFoundException) {
        }

        log.info { "Subscription with arn: $arn deleted" }

        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("UnsubscribeResponse") {
                    awsMetadata()
                })
        }
    }
}