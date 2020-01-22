package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.validateAndGet
import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.exception.SubscriptionNotFoundException
import io.github.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging

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
            xml("UnsubscribeResponse")
        }
    }
}