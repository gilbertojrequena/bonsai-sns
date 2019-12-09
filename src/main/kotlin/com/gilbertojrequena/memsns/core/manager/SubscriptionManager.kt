package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.SendChannel
import java.util.*

class SubscriptionManager(snsOpActor: SendChannel<SnsOpsMessage>) : SqsOperationsManager(snsOpActor) {

    suspend fun create(subscription: Subscription): Subscription {
        val topic = findTopicByArn(subscription.topicArn)

        return sendToActorAndReceive {
            SnsOpsMessage.SaveSubscription(
                Subscription(
                    subscription.topicArn,
                    subscription.protocol,
                    subscription.endpoint,
                    "owner",
                    "arn:memsns:sns:memsns-region:123456789:${topic.name}:${UUID.randomUUID()}"
                ), it
            )
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

    private suspend fun findTopicByArn(topicArn: TopicArn): Topic {
        return sendToActorAndReceive { SnsOpsMessage.FindTopicByArn(topicArn, it) }
    }

    suspend fun delete(arn: SubscriptionArn): Subscription? {
        return try {
            sendToActorAndReceive { SnsOpsMessage.DeleteSubscription(arn, it) }
        } catch (e: Exception) {
            null
        }
    }
}