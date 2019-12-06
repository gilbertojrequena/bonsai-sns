package com.gilbertojrequena.memsns.core.actor

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import com.gilbertojrequena.memsns.core.exception.SubscriptionNotFound
import com.gilbertojrequena.memsns.core.exception.TopicAlreadyExist
import com.gilbertojrequena.memsns.core.exception.TopicNotFoundException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.min

fun snsOpsActor() = GlobalScope.actor<SnsOpsMessage> {
    with(Database()) {
        for (message in channel) {
            when (message) {
                is SnsOpsMessage.SaveTopic -> sendOrClose(message.response) { saveTopic(message.topic) }
                is SnsOpsMessage.FindAllTopics -> message.response.send(findAllTopics(message.fromToken))
                is SnsOpsMessage.FindTopicByArn -> sendOrClose(message.response) { findTopicByArn(message.arn) }
                is SnsOpsMessage.TopicExists -> message.response.send(topicExists(message.arn))
                is SnsOpsMessage.DeleteTopic -> sendOrClose(message.response) { deleteTopic(message.arn) }
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

private suspend fun <E> sendOrClose(channel: SendChannel<E>, block: () -> E) {
    try {
        channel.send(block())
    } catch (e: Exception) {
        channel.close(e)
    }
}

private class Database {

    private val topics: MutableMap<TopicArn, TopicWithToken> = mutableMapOf()
    private val topicSubscriptions: MutableMap<SubscriptionKey, SubscriptionWithToken> = mutableMapOf()

    fun topicExists(arn: TopicArn): Boolean = topics.containsKey(arn)

    fun saveTopic(topic: Topic): Topic {
        if (topicExists(topic.arn)) {
            throw TopicAlreadyExist(topic.name)
        }
        topics[topic.arn] = TopicWithToken(topic, topic.arn.md5())
        return topic
    }

    fun deleteTopic(arn: TopicArn): Topic {
        return topics.remove(arn)?.topic ?: throw TopicNotFoundException(arn)
    }

    fun findAllTopics(fromToken: Token?): TopicsAndToken {
        return pagedFindAll(fromToken, topics.values,
            { elements -> elements.sortedBy { it.topic.name } },
            { twt -> twt.topic },
            { topics, nextToken -> TopicsAndToken(topics, nextToken) })
    }

    fun findTopicByArn(arn: TopicArn): Topic = topics[arn]?.topic ?: throw TopicNotFoundException(arn)

    fun subscriptionExists(subscription: Subscription) =
        topicSubscriptions.containsKey(subscription.key())

    fun saveSubscription(subscription: Subscription): Subscription {
        if (subscriptionExists(subscription)) {
            throw SubscriptionNotFound(subscription.arn)
        }
        topicSubscriptions[subscription.key()] =
            SubscriptionWithToken(subscription, subscription.key().md5())
        return subscription
    }

    fun findAllSubscriptions(fromToken: Token?): SubscriptionsAndToken {
        return pagedFindAll(fromToken, topicSubscriptions.values,
            { elements -> elements.sortedBy { it.subscription.key() } },
            { swt -> swt.subscription },
            { subscriptions, nextToken -> SubscriptionsAndToken(subscriptions, nextToken) })
    }

    fun findAllSubscriptionsByTopicArn(topicArn: TopicArn, fromToken: Token?): SubscriptionsAndToken {
        return pagedFindAll(fromToken, topicSubscriptions.values,
            { elements ->
                elements.sortedBy { swt -> swt.subscription.key() }
                    .filter { it.subscription.topicArn == topicArn }
            },
            { swt -> swt.subscription },
            { subscriptions, nextToken -> SubscriptionsAndToken(subscriptions, nextToken) })
    }

    fun deleteSubscription(arn: SubscriptionArn): Boolean {
        val subscriptionWithToken =
            topicSubscriptions.values.find { sub -> sub.subscription.arn == arn } ?: return false

        return topicSubscriptions.remove(subscriptionWithToken.subscription.key()) != null
    }

    fun <E : ElementWithToken, L, R> pagedFindAll(
        fromToken: Token?, elements: MutableCollection<E>,
        preProcess: (MutableCollection<E>) -> List<E>,
        listMapper: (E) -> L,
        resultMapper: (List<L>, Token?) -> R
    ): R {
        val preProcessedElements = preProcess(elements)
        val indexOfFirst = fromToken?.let { preProcessedElements.indexOfFirst { swt -> swt.token == fromToken } } ?: 0
        val nextTokenIndex = indexOfFirst + 100

        val resultList = preProcessedElements.subList(indexOfFirst, min(preProcessedElements.size, nextTokenIndex))
            .map(listMapper)

        val nextToken: Token? =
            if (nextTokenIndex < preProcessedElements.size) preProcessedElements[nextTokenIndex].token else null

        return resultMapper(resultList, nextToken)
    }

    private class TopicWithToken(val topic: Topic, token: Token) : ElementWithToken(token)

    private class SubscriptionWithToken(val subscription: Subscription, token: Token) : ElementWithToken(token)

    private open class ElementWithToken(val token: Token)

    private fun Subscription.key(): SubscriptionKey = this.topicArn + this.protocol + this.endpoint

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}