package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.exception.InvalidParameterException
import com.gilbertojrequena.memsns.api.validateAndGet
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class SetSubscriptionAttributes(private val subscriptionManager: SubscriptionManager) : Action {
    companion object {
        private val SUPPORTED_ATTRIBUTES = setOf(
            "DeliveryPolicy", "FilterPolicy", "RawMessageDelivery", "RedrivePolicy"
        )
    }

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val subscriptionArn = params.validateAndGet("SubscriptionArn")
        val attributeName = params.validateAndGet("AttributeName").let {
            if (!SUPPORTED_ATTRIBUTES.contains(it)) {
                throw InvalidParameterException(params.validateAndGet("AttributeName"), "AttributeName")
            }
            it
        }
        val attributeValue = params.validateAndGet("AttributeValue")
        subscriptionManager.setSubscriptionAttribute(subscriptionArn, attributeName, attributeValue)
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("SetSubscriptionAttributesResponse") {
                    awsMetadata()
                })
        }
    }
}