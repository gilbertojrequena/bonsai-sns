package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.createTopicSubscriptionData
import com.gilbertojrequena.memsns.api.exception.InvalidParameterException
import com.gilbertojrequena.memsns.core.exception.EndpointProtocolMismatchException
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class Subscribe(private val subscriptionManager: SubscriptionManager) : Action {

    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val subscription = try {
            subscriptionManager.create(params.createTopicSubscriptionData())
        } catch (e: EndpointProtocolMismatchException) {
            throw InvalidParameterException("Endpoint", "Endpoint must match the specified protocol")
        }
        log.info { "Subscription $subscription created" }
        call.respondText {
            writeXmlElement(
                jdom("SubscribeResponse") {
                    element("SubscribeResult") {
                        element("SubscriptionArn") {
                            text(subscription.arn)
                        }
                    }
                    awsMetadata()
                })
        }
    }
}

