package com.gilbertojrequena.memsns.core

typealias TopicArn = String
typealias SubscriptionArn = String
typealias Token = String
typealias Attribute = Pair<String, String>
typealias Attributes = Map<String, String>

data class Topic(
    val name: String,
    val arn: String = ""
)

data class Subscription(
    val topicArn: String,
    val protocol: Protocol,
    val endpoint: String,
    val owner: String = "",
    val arn: String = ""
) {
    enum class Protocol(val value: String) {
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

data class SubscriptionWithAttributes(val subscription: Subscription, val attributes: Attributes)

data class PublishRequest(val topicArn: String, val message: Message, val messageId: String = "")

data class TopicsAndToken(val topics: List<Topic>, val nextToken: Token?)

data class SubscriptionsAndToken(val subscriptions: List<Subscription>, val nextToken: String?)

data class MessageAttribute(val type: String, val value: String)

data class Message(val body: String, val attributes: Map<String, MessageAttribute> = mapOf())