package com.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.bonsai_sns.api.awsMetadata
import com.gilbertojrequena.bonsai_sns.api.validateAndGet
import com.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class ListSubscriptionsByTopic(private val subscriptionManager: SubscriptionManager) :
    Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val subscriptionsAndToken =
            subscriptionManager.findAllByTopicArn(params.validateAndGet("TopicArn"), params["NextToken"])

        call.respondText {
            writeXmlElement(jdom("ListSubscriptionsByTopicResponse") {
                element("ListSubscriptionsByTopicResult") {
                    if (subscriptionsAndToken.nextToken != null) {
                        element("NextToken") {
                            text(subscriptionsAndToken.nextToken)
                        }
                    }
                    element("Subscriptions") {
                        for (subscription in subscriptionsAndToken.subscriptions) {
                            element("member") {
                                element("TopicArn") {
                                    text(subscription.topicArn)
                                }
                                element("Protocol") {
                                    text(subscription.protocol.name)
                                }
                                element("SubscriptionArn") {
                                    text(subscription.arn)
                                }
                                element("Owner") {
                                    text(subscription.owner)
                                }
                                element("Endpoint") {
                                    text(subscription.endpoint)
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

