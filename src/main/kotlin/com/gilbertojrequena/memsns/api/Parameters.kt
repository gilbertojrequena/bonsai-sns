package com.gilbertojrequena.memsns.api

import com.gilbertojrequena.memsns.api.*
import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import io.ktor.http.Parameters
import kotlinx.coroutines.channels.SendChannel


fun Parameters.createTopicData(): Topic = Topic(this["Name"]!!, this["Name"]!!) // TODO Validate
fun Parameters.createTopicSubscriptionData(): Subscription = Subscription(
    this["TopicArn"]!!,
    Subscription.Protocol.fromName(this["Protocol"]!!), this["Endpoint"]!!
) // TODO Validate

fun Parameters.createPublishRequest() = PublishRequest(this["TopicArn"]!!, this["Message"]!!) // TODO Validate

fun Parameters.action(): String {
    return this["Action"] ?: throw TODO("Validate")
}