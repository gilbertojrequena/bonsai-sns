package com.gilbertojrequena.bonsai_sns.core.manager

import com.fasterxml.jackson.core.JsonParseException
import com.gilbertojrequena.bonsai_sns.core.*
import com.gilbertojrequena.bonsai_sns.core.Subscription.Protocol.*
import com.gilbertojrequena.bonsai_sns.core.actor.message.SnsOpsMessage
import com.gilbertojrequena.bonsai_sns.core.exception.EndpointProtocolMismatchException
import com.gilbertojrequena.bonsai_sns.core.exception.InvalidQueueArnException
import com.gilbertojrequena.bonsai_sns.core.exception.InvalidRedrivePolicyException
import com.gilbertojrequena.bonsai_sns.core.filter_policy.PolicyFilterValidator
import com.gilbertojrequena.bonsai_sns.server.BonsaiSnsConfig
import kotlinx.coroutines.channels.SendChannel
import java.lang.Integer.toHexString

internal class SubscriptionManager(snsOpActor: SendChannel<SnsOpsMessage>, private val config: BonsaiSnsConfig) :
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
        when (subscription.protocol) {
            HTTP, HTTPS -> {
                if (!subscription.endpoint.startsWith("${subscription.protocol.value}://")) {
                    throw EndpointProtocolMismatchException(
                        subscription.endpoint,
                        subscription.protocol.value
                    )
                }
            }
            SQS -> validateQueueArn(subscription.endpoint)
        }
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
            policyValidator.validate(attributeValue)
        } else if (attributeName == "RedrivePolicy") {
            try {
                val dlqArn = JsonMapper.instance().read(attributeValue).get("deadLetterTargetArn").textValue()
                validateQueueArn(dlqArn)
            } catch (e: JsonParseException) {
                throw InvalidRedrivePolicyException(e.message!!)
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

    private fun validateQueueArn(arn: String) {
        if (!arn.matches(Regex("arn:aws:sqs:[a-z0-9-]+:\\d+:[a-zA-Z0-9-_]+$"))) {
            throw InvalidQueueArnException(arn)
        }
    }

    private fun buildSubscriptionHash(subscription: Subscription): String {
        return "${toHexString(subscription.topicArn.hashCode())}-${toHexString(subscription.protocol.hashCode())}-${toHexString(
            subscription.endpoint.hashCode()
        )}"
    }
}