package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.validateAndGet
import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText

internal class GetSubscriptionAttributes(private val subscriptionManager: SubscriptionManager) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val attributes =
            subscriptionManager.findSubscriptionAttributes(params.validateAndGet("SubscriptionArn"))

        call.respondText {
            xml("GetSubscriptionAttributesResponse") {
                element("GetSubscriptionAttributesResult") {
                    element("Attributes") {
                        for (attribute in attributes) {
                            element("entry") {
                                element("key") {
                                    text = attribute.key
                                }
                                element("value") {
                                    text = attribute.value
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}