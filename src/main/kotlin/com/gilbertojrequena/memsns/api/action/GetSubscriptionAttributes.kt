package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.validateAndGet
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class GetSubscriptionAttributes(private val subscriptionManager: SubscriptionManager) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val attributes =
            subscriptionManager.findSubscriptionAttributes(params.validateAndGet("SubscriptionArn"))

        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("GetSubscriptionAttributesResponse") {
                    element("GetSubscriptionAttributesResult") {
                        element("Attributes") {
                            for (attribute in attributes) {
                                element("entry") {
                                    element("key") {
                                        text(attribute.key)
                                    }
                                    element("value") {
                                        text(attribute.value)
                                    }
                                }
                            }
                        }
                    }
                    awsMetadata()
                })
        }
    }
}