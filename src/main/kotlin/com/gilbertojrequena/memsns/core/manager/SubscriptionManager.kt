package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import com.gilbertojrequena.memsns.server.MemSnsConfig
import kotlinx.coroutines.channels.SendChannel
import java.lang.Integer.toHexString

class SubscriptionManager(snsOpActor: SendChannel<SnsOpsMessage>, private val config: MemSnsConfig) :
    SqsOperationsManager(snsOpActor) {

    suspend fun create(subscription: Subscription): Subscription {
        val topic = findTopicByArn(subscription.topicArn)

        return sendToActorAndReceive {
            SnsOpsMessage.SaveSubscription(
                Subscription(
                    subscription.topicArn,
                    subscription.protocol,
                    subscription.endpoint,
                    "owner",
                    "arn:aws:sns:${config.region}:${config.accountId}:${topic.name}:${buildSubscriptionHash(subscription)}"
                ), it
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