package io.github.gilbertojrequena.bonsai_sns.core.actor.dispatcher

import io.github.gilbertojrequena.bonsai_sns.core.Message
import io.github.gilbertojrequena.bonsai_sns.core.SubscriptionArn

internal interface MessageDispatcher {
    suspend fun dispatch(dispatchMessageRequest: DispatchMessageRequest)
//    suspend fun dispatch(endpoint: String, publishRequest: PublishRequest, isRaw: Boolean)
}

internal data class DispatchMessageRequest(
    val endpoint: String,
    val topicArn: String,
    val message: Message,
    val messageId: String,
    val subscriptionArn: SubscriptionArn,
    val isRaw: Boolean
)
