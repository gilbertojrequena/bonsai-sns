package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.RetriableHttpClient
import mu.KotlinLogging

internal class HttpMessageDispatcher(private val httpClient: RetriableHttpClient) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun dispatch(endpoint: String, message: String) {
        log.debug { "Posting: '$message' to endpoint: '$endpoint'" }
        httpClient.post(endpoint, message)
        log.debug { "Finished posting: '$message' to endpoint: '$message'" }
    }
}