package com.gilbertojrequena.memsns.core

typealias TopicArn = String
typealias SubscriptionArn = String
typealias SubscriptionKey = String
typealias Token = String

data class Topic(
    val name: String,
    val displayName: String,
    val deliveryRetry: DeliveryRetry = DeliveryRetry(),
    val arn: String = "",
    val attributes: List<Attribute> = listOf(),
    val tags: Map<String, String> = mapOf()
) {
    data class DeliveryRetry(
        val numberOfRetries: Int = 3,
        val retriesWithoutDelay: Int = 0,
        val minimumDelay: Int = 20,
        val maximumDelay: Int = 10,
        val minimumDelayRetries: Int = 0,
        val maximumDelayRetries: Int = 0,
        val maximumReceiveRate: Int? = null,
        val retryBackOffFunction: RetryBackOffFunction = RetryBackOffFunction.LINEAR
    ) {

        enum class RetryBackOffFunction(name: String) {
            LINEAR("Linear"),
            ARITHMETIC("Arithmetic"),
            GEOMETRIC("Geometric"),
            EXPONENTIAL("Exponential")
        }
    }

    data class Attribute(val key: String, val value: String)
}

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

data class PublishRequest(val topicArn: String, val message: String, val messageId: String = "")

data class TopicsAndToken(val topics: List<Topic>, val nextToken: Token?)

data class SubscriptionsAndToken(val subscriptions: List<Subscription>, val nextToken: String?)