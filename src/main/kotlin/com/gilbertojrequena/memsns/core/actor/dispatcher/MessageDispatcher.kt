package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.Topic

interface MessageDispatcher {
    suspend fun publish(subscription: Subscription, message: String, messageId: String)
}