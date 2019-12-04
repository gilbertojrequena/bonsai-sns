package com.gilbertojrequena.memsns.core.actor

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.min

fun snsOpsActor() = GlobalScope.actor<SnsOpsMessage> {
    with(Database()) {
        for (message in channel) {
            when (message) {
                is SnsOpsMessage.SaveTopic -> message.response.send(saveTopic(message.topic))
                is SnsOpsMessage.FindAllTopics -> message.response.send(findAllTopics(message.fromToken))
                is SnsOpsMessage.FindTopicByArn -> message.response.send(findTopicByArn(message.arn))
                is SnsOpsMessage.TopicExists -> message.response.send(topicExists(message.arn))
                is SnsOpsMessage.DeleteTopic -> message.response.send(deleteTopic(message.arn))
                is SnsOpsMessage.FindAllSubscriptions -> message.response.send(findAllSubscriptions(message.fromToken))
                is SnsOpsMessage.SaveSubscription -> message.response.send(saveSubscription(message.subscription))
                is SnsOpsMessage.FindAllSubscriptionsByTopic -> message.response.send(
                    findAllSubscriptionsByTopicArn(
                        message.topicArn,
                        message.fromToken
                    )
                )
                is SnsOpsMessage.DeleteSubscription -> message.response.send(deleteSubscription(message.arn))
            }
        }
    }
}

private class Database {

    private val topics: MutableMap<TopicArn, TopicWithToken> = mutableMapOf()
    private val topicSubscriptions: MutableMap<SubscriptionKey, SubscriptionWithToken> = mutableMapOf()

    fun topicExists(arn: TopicArn): Boolean = topics.containsKey(arn)

    fun saveTopic(topic: Topic): Topic {
        if (topicExists(topic.arn)) {
            throw TODO()
        }
        topics[topic.arn] = TopicWithToken(topic, topic.arn.md5())
        return topic
    }

    fun deleteTopic(arn: TopicArn): Boolean {
        return topics.remove(arn) != null
    }

    fun findAllTopics(fromToken: Token?): TopicsAndToken {
//        TODO This logic is duplicated
        val sortedTopics = topics.values.sortedBy { it.topic.name }

        val indexOfFirst = fromToken?.let { sortedTopics.indexOfFirst { twt -> twt.token == fromToken } } ?: 0
        val nextTokenIndex = indexOfFirst + 100
        val topicList =
            sortedTopics.subList(indexOfFirst, min(sortedTopics.size, nextTokenIndex)).map { twt -> twt.topic }
        val nextToken = if (nextTokenIndex < sortedTopics.size) sortedTopics[nextTokenIndex].token else null

        return TopicsAndToken(topicList, nextToken)
    }

    fun findTopicByArn(arn: TopicArn): Topic? = topics[arn]?.topic

    fun subscriptionExists(subscription: Subscription) =
        topicSubscriptions.containsKey(subscription.key())

    fun saveSubscription(subscription: Subscription): Subscription {
        if (subscriptionExists(subscription)) {
            throw TODO()
        }
        topicSubscriptions[subscription.key()] =
            SubscriptionWithToken(subscription, subscription.key().md5())
        return subscription
    }

    fun findAllSubscriptions(fromToken: Token?): SubscriptionsAndToken {
        val sortedSubs = topicSubscriptions.values.sortedBy { swt -> swt.subscription.key() }
        val indexOfFirst = fromToken?.let { sortedSubs.indexOfFirst { swt -> swt.token == fromToken } } ?: 0
        val nextTokenIndex = indexOfFirst + 100

        val subscriptionsList = sortedSubs.subList(indexOfFirst, min(sortedSubs.size, nextTokenIndex))
            .map { swt -> swt.subscription }

        val nextToken =
            if (nextTokenIndex < sortedSubs.size) sortedSubs[nextTokenIndex].token else null

        return SubscriptionsAndToken(subscriptionsList, nextToken)
    }

    fun findAllSubscriptionsByTopicArn(topicArn: TopicArn, fromToken: Token?): SubscriptionsAndToken {
        val sortedSubs = topicSubscriptions.values.sortedBy { swt -> swt.subscription.key() }
            .filter { it.subscription.topicArn == topicArn }
        val indexOfFirst = fromToken?.let { sortedSubs.indexOfFirst { swt -> swt.token == fromToken } } ?: 0
        val nextTokenIndex = indexOfFirst + 100

        val subscriptionsList = sortedSubs.subList(indexOfFirst, min(sortedSubs.size, nextTokenIndex))
            .map { swt -> swt.subscription }

        val nextToken =
            if (nextTokenIndex < sortedSubs.size) sortedSubs[nextTokenIndex].token else null

        return SubscriptionsAndToken(subscriptionsList, nextToken)
    }

    fun deleteSubscription(arn: SubscriptionArn): Boolean {
        val subscriptionWithToken =
            topicSubscriptions.values.find { sub -> sub.subscription.arn == arn } ?: return false

        return topicSubscriptions.remove(subscriptionWithToken.subscription.key()) != null
    }

    private data class TopicWithToken(val topic: Topic, val token: Token)

    private data class SubscriptionWithToken(val subscription: Subscription, val token: Token)

    private fun Subscription.key(): SubscriptionKey = this.topicArn + this.protocol + this.endpoint

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}