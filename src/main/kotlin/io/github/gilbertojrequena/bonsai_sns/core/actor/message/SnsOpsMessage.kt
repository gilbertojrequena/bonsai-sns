package io.github.gilbertojrequena.bonsai_sns.core.actor.message

import io.github.gilbertojrequena.bonsai_sns.core.*
import kotlinx.coroutines.channels.SendChannel

internal sealed class SnsOpsMessage {
    class SaveTopic(val topic: Topic, val response: SendChannel<Topic>) : SnsOpsMessage()
    class FindAllTopics(val fromToken: Token? = null, val response: SendChannel<TopicsAndToken>) :
        SnsOpsMessage()

    class FindTopicByArn(val arn: TopicArn, val response: SendChannel<Topic>) : SnsOpsMessage()
    class TopicExists(val arn: TopicArn, val response: SendChannel<Boolean>) : SnsOpsMessage()
    class DeleteTopic(val arn: TopicArn, val response: SendChannel<Topic>) : SnsOpsMessage()
    class FindAllSubscriptions(val fromToken: Token? = null, val response: SendChannel<SubscriptionsAndToken>) :
        SnsOpsMessage()

    class SaveSubscription(val subscription: Subscription, val response: SendChannel<Subscription>) :
        SnsOpsMessage()

    class FindSubscriptionByArn(val arn: SubscriptionArn, val response: SendChannel<Subscription>) : SnsOpsMessage()
    class FindAllSubscriptionsByTopic(
        val topicArn: TopicArn,
        val fromToken: Token? = null,
        val response: SendChannel<SubscriptionsAndToken>
    ) :
        SnsOpsMessage()

    class DeleteSubscription(val arn: SubscriptionArn, val response: SendChannel<Subscription>) : SnsOpsMessage()
    class SetSubscriptionAttribute(
        val arn: SubscriptionArn,
        val attribute: Attribute,
        val response: SendChannel<Attribute>
    ) : SnsOpsMessage()

    class GetSubscriptionAttributes(
        val arn: SubscriptionArn,
        val response: SendChannel<Attributes>
    ) : SnsOpsMessage()
}