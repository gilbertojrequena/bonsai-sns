package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import java.util.*

class SubscriptionManager(private val snsOpActor: SendChannel<SnsOpsMessage>) {

    suspend fun create(subscription: Subscription): Subscription {
        val topic = findTopicByArn(subscription.topicArn) ?: TODO("Exception")

        val responseChannel = Channel<Subscription>()
        snsOpActor.send(
            SnsOpsMessage.SaveSubscription(
                Subscription(
                    subscription.topicArn,
                    subscription.protocol,
                    subscription.endpoint,
                    "owner",
                    "arn:memsns:sns:memsns-region:123456789:${topic.name}:${UUID.randomUUID()}"
                ), responseChannel
            )
        )
        return responseChannel.receive()
    }

    suspend fun findAll(nextToken: Token?): SubscriptionsAndToken {
        val responseChannel = Channel<SubscriptionsAndToken>()
        snsOpActor.send(SnsOpsMessage.FindAllSubscriptions(nextToken, responseChannel))
        return responseChannel.receive()
    }

    suspend fun findAllByTopicArn(topicArn: TopicArn, token: Token?): SubscriptionsAndToken {
        val responseChannel = Channel<SubscriptionsAndToken>()
        snsOpActor.send(
            SnsOpsMessage.FindAllSubscriptionsByTopic(
                topicArn,
                token,
                responseChannel
            )
        )
        return responseChannel.receive()
    }

    private suspend fun findTopicByArn(topicArn: TopicArn): Topic? {
        val responseChannel = Channel<Topic?>()
        snsOpActor.send(SnsOpsMessage.FindTopicByArn(topicArn, responseChannel))
        return responseChannel.receive()
    }

    suspend fun delete(arn: SubscriptionArn): Boolean {
        val responseChannel = Channel<Boolean>()
        snsOpActor.send(SnsOpsMessage.DeleteSubscription(arn, responseChannel))
        return responseChannel.receive()
    }
}