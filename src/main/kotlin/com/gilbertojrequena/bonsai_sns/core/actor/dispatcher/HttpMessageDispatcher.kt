package com.gilbertojrequena.bonsai_sns.core.actor.dispatcher

import com.gilbertojrequena.bonsai_sns.core.RetriableHttpClient
import com.gilbertojrequena.bonsai_sns.core.exception.MessageDispatchException
import mu.KotlinLogging

internal class HttpMessageDispatcher(private val httpClient: RetriableHttpClient) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun dispatch(endpoint: String, message: String) {
        log.debug { "Posting: '$message' to endpoint: '$endpoint'" }
        try {
            httpClient.post(endpoint, message)
        } catch (e: Exception) {
            throw MessageDispatchException(endpoint, message)
        }
        log.debug { "Finished posting: '$message' to endpoint: '$message'" }
    }
}