package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText

internal class ListSubscriptions(private val subscriptionManager: SubscriptionManager) :
    Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val subscriptionsAndToken = subscriptionManager.findAll(params["NextToken"])


        call.respondText {
            xml("ListSubscriptionsResponse") {
                element("ListSubscriptionsResult") {
                    if (subscriptionsAndToken.nextToken != null) {
                        element("NextToken") {
                            text = subscriptionsAndToken.nextToken
                        }
                    }
                    element("Subscriptions") {
                        for (subscription in subscriptionsAndToken.subscriptions) {
                            element("member") {
                                element("TopicArn") {
                                    text = subscription.topicArn
                                }
                                element("Protocol") {
                                    text = subscription.protocol.name
                                }
                                element("SubscriptionArn") {
                                    text = subscription.arn
                                }
                                element("Owner") {
                                    text = subscription.owner
                                }
                                element("Endpoint") {
                                    text = subscription.endpoint
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

