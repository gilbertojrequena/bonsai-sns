package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.SnsHttpClient
import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.Topic
import mu.KotlinLogging

public class HttpMessageDispatcher(private val httpClient: SnsHttpClient) :
    MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun publish(topic: Topic, subscription: Subscription, message: String, messageId: String) {
        log.debug { "Publishing: <$message> to subscription: $subscription" }
        httpClient.post(subscription.endpoint, message, topic.deliveryRetry)
        log.debug { "Finished publishing: <$message> to subscription: $subscription" }
    }
}