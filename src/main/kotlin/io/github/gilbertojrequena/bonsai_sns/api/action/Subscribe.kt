package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.createTopicSubscriptionData
import io.github.gilbertojrequena.bonsai_sns.api.exception.InvalidParameterException
import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.exception.EndpointProtocolMismatchException
import io.github.gilbertojrequena.bonsai_sns.core.exception.InvalidQueueArnException
import io.github.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import mu.KotlinLogging

internal class Subscribe(private val subscriptionManager: SubscriptionManager) : Action {

    private val log = KotlinLogging.logger {}

    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val subscription = try {
            subscriptionManager.create(params.createTopicSubscriptionData())
        } catch (e: EndpointProtocolMismatchException) {
            throw InvalidParameterException("Endpoint", "Endpoint must match the specified protocol")
        } catch (e: InvalidQueueArnException) {
            throw InvalidParameterException("QueueArn", "SQS endpoint ARN")
        }
        log.info { "Subscription $subscription created" }
        call.respondText {
            xml("SubscribeResponse") {
                element("SubscribeResult") {
                    element("SubscriptionArn") {
                        text = subscription.arn
                    }
                }
            }
        }
    }
}

