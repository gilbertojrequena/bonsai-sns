package io.github.gilbertojrequena.bonsai_sns.core.actor

import io.github.gilbertojrequena.bonsai_sns.core.*
import io.github.gilbertojrequena.bonsai_sns.core.actor.message.SnsOpsMessage
import io.github.gilbertojrequena.bonsai_sns.core.exception.SubscriptionNotFoundException
import io.github.gilbertojrequena.bonsai_sns.core.exception.TopicNotFoundException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.min

internal fun snsOpsActor() = GlobalScope.actor<SnsOpsMessage> {
    with(Database()) {
        for (message in channel) {
            when (message) {
                is SnsOpsMessage.SaveTopic -> sendOrClose(message.response) { saveTopic(message.topic) }
                is SnsOpsMessage.FindAllTopics -> message.response.send(findAllTopics(message.fromToken))
                is SnsOpsMessage.FindTopicByArn -> sendOrClose(message.response) { findTopicByArn(message.arn) }
                is SnsOpsMessage.TopicExists -> message.response.send(topicExists(message.arn))
                is SnsOpsMessage.DeleteTopic -> sendOrClose(message.response) { deleteTopic(message.arn) }
                is SnsOpsMessage.FindSubscriptionByArn -> sendOrClose(message.response) { findSubscriptionByArn(message.arn) }
                is SnsOpsMessage.FindAllSubscriptions -> message.response.send(findAllSubscriptions(message.fromToken))
                is SnsOpsMessage.SaveSubscription -> sendOrClose(message.response) { saveSubscription(message.subscription) }
                is SnsOpsMessage.FindAllSubscriptionsByTopic -> sendOrClose(message.response) {
                    findAllSubscriptionsByTopicArn(
                        message.topicArn,
                        message.fromToken
                    )
                }
                is SnsOpsMessage.DeleteSubscription -> sendOrClose(message.response) { deleteSubscription(message.arn) }
                is SnsOpsMessage.SetSubscriptionAttribute -> sendOrClose(message.response) {
                    saveSubscriptionAttribute(
                        message.arn,
                        message.attribute
                    )
                }
                is SnsOpsMessage.GetSubscriptionAttributes -> message.response.send(getSubscriptionAttributes(message.arn))
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
    private val subscriptions: MutableMap<SubscriptionArn, SubscriptionWithToken> = mutableMapOf()
    private val subscriptionAttributes: MutableMap<SubscriptionArn, Map<String, String>> = mutableMapOf()

    fun topicExists(arn: TopicArn): Boolean = topics.containsKey(arn)

    fun saveTopic(topic: Topic): Topic {
        if (topicExists(topic.arn)) {
            return topic
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

    fun saveSubscription(subscription: Subscription): Subscription {
        subscriptions[subscription.arn] =
            SubscriptionWithToken(subscription, subscription.arn.md5())
        return subscription
    }

    fun findSubscriptionByArn(arn: SubscriptionArn): Subscription {
        return (subscriptions[arn] ?: throw SubscriptionNotFoundException(arn)).subscription
    }

    fun findAllSubscriptions(fromToken: Token?): SubscriptionsAndToken {
        return pagedFindAll(fromToken, subscriptions.values,
            { elements -> elements.sortedBy { it.subscription.arn } },
            { swt -> swt.subscription },
            { subscriptions, nextToken -> SubscriptionsAndToken(subscriptions, nextToken) })
    }

    fun findAllSubscriptionsByTopicArn(topicArn: TopicArn, fromToken: Token?): SubscriptionsAndToken {
        val topic = findTopicByArn(topicArn)
        return pagedFindAll(fromToken, subscriptions.values,
            { elements ->
                elements.sortedBy { swt -> swt.subscription.arn }
                    .filter { it.subscription.topicArn == topic.arn }
            },
            { swt -> swt.subscription },
            { subscriptions, nextToken -> SubscriptionsAndToken(subscriptions, nextToken) })
    }

    fun deleteSubscription(arn: SubscriptionArn): Subscription {
        val subscriptionWithToken =
            subscriptions.values.find { sub -> sub.subscription.arn == arn }
                ?: throw SubscriptionNotFoundException(arn)

        subscriptionAttributes.remove(subscriptionWithToken.subscription.arn)
        return subscriptions.remove(subscriptionWithToken.subscription.arn)!!.subscription
    }

    fun saveSubscriptionAttribute(arn: SubscriptionArn, attribute: Attribute): Attribute {
        subscriptionAttributes.computeIfAbsent(arn) { mapOf(attribute) }
        subscriptionAttributes.computeIfPresent(arn) { _, attributes -> attributes.plus(attribute) }
        return attribute
    }

    fun getSubscriptionAttributes(arn: SubscriptionArn): Attributes {
        return subscriptionAttributes.getOrDefault(arn, mapOf())
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

    private fun String.md5(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }
}