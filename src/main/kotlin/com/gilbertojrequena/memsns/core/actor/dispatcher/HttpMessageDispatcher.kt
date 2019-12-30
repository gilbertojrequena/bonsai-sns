package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.RetriableHttpClient
import com.gilbertojrequena.memsns.core.exception.MessageDispatchException
import com.gilbertojrequena.memsns.core.exception.UnsuccessfulHttpCallException
import mu.KotlinLogging

internal class HttpMessageDispatcher(private val httpClient: RetriableHttpClient) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun dispatch(endpoint: String, message: String) {
        log.debug { "Posting: '$message' to endpoint: '$endpoint'" }
        try {
            httpClient.post(endpoint, message)
        } catch (e: UnsuccessfulHttpCallException) {
            throw MessageDispatchException(endpoint, message)
        }
        log.debug { "Finished posting: '$message' to endpoint: '$message'" }
    }
}