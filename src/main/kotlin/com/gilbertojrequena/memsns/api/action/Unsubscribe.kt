package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.validateAndGet
import com.gilbertojrequena.memsns.core.exception.SubscriptionNotFoundException
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
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