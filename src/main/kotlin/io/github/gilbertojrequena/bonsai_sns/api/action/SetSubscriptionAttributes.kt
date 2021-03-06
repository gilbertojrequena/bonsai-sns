package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.exception.InvalidParameterException
import io.github.gilbertojrequena.bonsai_sns.api.validateAndGet
import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.exception.InvalidFilterPolicyException
import io.github.gilbertojrequena.bonsai_sns.core.exception.InvalidQueueArnException
import io.github.gilbertojrequena.bonsai_sns.core.exception.InvalidRedrivePolicyException
import io.github.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText

internal class SetSubscriptionAttributes(private val subscriptionManager: SubscriptionManager) : Action {
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
        try {
            subscriptionManager.setSubscriptionAttribute(subscriptionArn, attributeName, attributeValue)
        } catch (e: InvalidQueueArnException) {
            throw InvalidParameterException("QueueArn", "SQS endpoint ARN")
        } catch (e: InvalidFilterPolicyException) {
            throw InvalidParameterException("FilterPolicy", e.message!!)
        } catch (e: InvalidRedrivePolicyException) {
            throw InvalidParameterException("RedrivePolicy", e.message!!)
        }
        call.respondText {
            xml("SetSubscriptionAttributesResponse")
        }
    }
}