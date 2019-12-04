package com.gilbertojrequena.memsns.core.actor.message

import com.gilbertojrequena.memsns.core.Subscription

sealed class PublishMessage(val subscription: Subscription, val message: String, val messageId: String) {
    class Publish(subscription: Subscription, message: String, messageId: String) :
        PublishMessage(subscription, message, messageId)
}