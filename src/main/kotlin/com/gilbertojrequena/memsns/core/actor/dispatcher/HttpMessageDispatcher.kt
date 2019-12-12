package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.SnsHttpClient
import com.gilbertojrequena.memsns.core.Subscription
import mu.KotlinLogging

public class HttpMessageDispatcher(private val httpClient: SnsHttpClient) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun publish(subscription: Subscription, message: String, messageId: String) {
        log.debug { "Publishing: <$message> to subscription: $subscription" }
        httpClient.post(subscription.endpoint, message)
        log.debug { "Finished publishing: <$message> to subscription: $subscription" }
    }
}