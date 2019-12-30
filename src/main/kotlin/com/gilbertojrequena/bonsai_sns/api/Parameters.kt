package com.gilbertojrequena.bonsai_sns.api

import com.gilbertojrequena.bonsai_sns.api.exception.InvalidParameterException
import com.gilbertojrequena.bonsai_sns.core.Message
import com.gilbertojrequena.bonsai_sns.core.PublishRequest
import com.gilbertojrequena.bonsai_sns.core.Subscription
import com.gilbertojrequena.bonsai_sns.core.Topic
import io.ktor.http.Parameters

internal fun Parameters.validateAndGet(parameter: String, reason: String = parameter): String {
    val value = this[parameter] ?: throw InvalidParameterException(parameter, reason)
    if (value.trim().isEmpty()) {
        throw InvalidParameterException(parameter, reason)
    }
    return value
}

internal fun Parameters.createTopicData(): Topic = Topic(this.validateAndGet("Name", "Topic Name"))
internal fun Parameters.createTopicSubscriptionData(): Subscription = Subscription(
    this.validateAndGet("TopicArn"),
    Subscription.Protocol.fromName(this.validateAndGet("Protocol")), this.validateAndGet("Endpoint")
)

internal fun Parameters.createPublishRequest(): PublishRequest {
    val messageAttributes = MessageAttributeParser.parse(this)
    val message = Message(this.validateAndGet("Message", "Empty Message"), messageAttributes)
    return PublishRequest(this.validateAndGet("TopicArn"), message)
}

internal fun Parameters.action(): String {
    return this.validateAndGet("Action")
}