package com.gilbertojrequena.memsns.core

internal typealias TopicArn = String
internal typealias SubscriptionArn = String
internal typealias Token = String
internal typealias Attribute = Pair<String, String>
internal typealias Attributes = Map<String, String>

internal data class Topic(
    val name: String,
    val arn: String = ""
)

internal data class Subscription(
    val topicArn: String,
    val protocol: Protocol,
    val endpoint: String,
    val owner: String = "",
    val arn: String = ""
) {
    internal enum class Protocol(val value: String) {
        HTTP("http"),
        HTTPS("https"),
        EMAIL("email"),
        EMAIL_JSON("email-json"),
        SMS("sms"),
        SQS("sqs"),
        APPLICATION("application"),
        LAMBDA("lambda");

        companion object {
            fun fromName(name: String): Protocol = values().first { p -> p.value == name }
        }
    }
}

internal data class SubscriptionWithAttributes(val subscription: Subscription, val attributes: Attributes)

internal data class PublishRequest(val topicArn: String, val message: Message, val messageId: String = "")

internal data class TopicsAndToken(val topics: List<Topic>, val nextToken: Token?)

internal data class SubscriptionsAndToken(val subscriptions: List<Subscription>, val nextToken: String?)

internal data class MessageAttribute(val type: String, val value: String)

internal data class Message(val body: String, val attributes: Map<String, MessageAttribute> = mapOf())