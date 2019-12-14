package com.gilbertojrequena.memsns.api

import com.gilbertojrequena.memsns.api.exception.InvalidParameterException
import com.gilbertojrequena.memsns.core.PublishRequest
import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.Topic
import io.ktor.http.Parameters

fun Parameters.validateAndGet(parameter: String, reason: String = parameter): String {
    val value = this[parameter] ?: throw InvalidParameterException(parameter, reason)
    if (value.trim().isEmpty()) {
        throw InvalidParameterException(parameter, reason)
    }
    return value
}

fun Parameters.createTopicData(): Topic = Topic(this.validateAndGet("Name", "Topic Name"))
fun Parameters.createTopicSubscriptionData(): Subscription = Subscription(
    this.validateAndGet("TopicArn"),
    Subscription.Protocol.fromName(this.validateAndGet("Protocol")), this.validateAndGet("Endpoint")
)

fun Parameters.createPublishRequest() = PublishRequest(this.validateAndGet("TopicArn"), this.validateAndGet("Message", "Empty Message"))

fun Parameters.action(): String {
    return this.validateAndGet("Action")
}