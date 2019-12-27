package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.api.exception.InvalidParameterException
import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.Subscription.Protocol.HTTP
import com.gilbertojrequena.memsns.core.Subscription.Protocol.HTTPS
import com.gilbertojrequena.memsns.core.filter_policy.PolicyFilterValidator
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import com.gilbertojrequena.memsns.core.exception.EndpointProtocolMismatchException
import com.gilbertojrequena.memsns.core.exception.InvalidFilterPolicyException
import com.gilbertojrequena.memsns.server.MemSnsConfig
import kotlinx.coroutines.channels.SendChannel
import java.lang.Integer.toHexString

internal class SubscriptionManager(snsOpActor: SendChannel<SnsOpsMessage>, private val config: MemSnsConfig) :
    SqsOperationsManager(snsOpActor) {

    private val policyValidator = PolicyFilterValidator()

    suspend fun create(subscription: Subscription): Subscription {
        val topic = findTopicByArn(subscription.topicArn)
        validateEndpoint(subscription)
        return sendToActorAndReceive {
            SnsOpsMessage.SaveSubscription(
                Subscription(
                    subscription.topicArn,
                    subscription.protocol,
                    subscription.endpoint,
                    config.accountId.toString(),
                    "arn:aws:sns:${config.region}:${config.accountId}:${topic.name}:${buildSubscriptionHash(subscription)}"
                ), it
            )
        }
    }

    private fun validateEndpoint(subscription: Subscription) {
        if (setOf(HTTP, HTTPS).contains(subscription.protocol) &&
            !subscription.endpoint.startsWith("${subscription.protocol.value}://")
        ) {
            throw EndpointProtocolMismatchException(
                subscription.endpoint,
                subscription.protocol.value
            )
        }
    }

    private fun buildSubscriptionHash(subscription: Subscription): String {
        return "${toHexString(subscription.topicArn.hashCode())}-${toHexString(subscription.protocol.hashCode())}-${toHexString(
            subscription.endpoint.hashCode()
        )}"
    }

    suspend fun findAll(nextToken: Token? = null): SubscriptionsAndToken {
        return sendToActorAndReceive { SnsOpsMessage.FindAllSubscriptions(nextToken, it) }
    }

    suspend fun findAllByTopicArn(topicArn: TopicArn, token: Token? = null): SubscriptionsAndToken {
        return sendToActorAndReceive {
            SnsOpsMessage.FindAllSubscriptionsByTopic(
                topicArn,
                token, it
            )
        }
    }

    suspend fun delete(arn: SubscriptionArn): Subscription {
        return sendToActorAndReceive { SnsOpsMessage.DeleteSubscription(arn, it) }
    }

    suspend fun setSubscriptionAttribute(
        arn: SubscriptionArn,
        attributeName: String,
        attributeValue: String
    ): Attribute {
        if (attributeName == "FilterPolicy") {
            try {
                policyValidator.validate(attributeValue)
            } catch (e: InvalidFilterPolicyException) {
                throw InvalidParameterException("FilterPolicy", e.message!!)
            }
        }
        return sendToActorAndReceive {
            SnsOpsMessage.SetSubscriptionAttribute(
                arn, attributeName to attributeValue, it
            )
        }
    }

    suspend fun findSubscriptionAttributes(arn: SubscriptionArn): Attributes {
        return sendToActorAndReceive {
            SnsOpsMessage.GetSubscriptionAttributes(arn, it)
        }
    }

    private suspend fun findTopicByArn(topicArn: TopicArn): Topic {
        return sendToActorAndReceive { SnsOpsMessage.FindTopicByArn(topicArn, it) }
    }
}