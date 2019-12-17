package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.SnsHttpClient
import mu.KotlinLogging

class SqsMessageDispatcher(private val httpClient: SnsHttpClient) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun dispatch(endpoint: String, message: String) {
        log.debug { "Posting: <$message> to endpoint: $endpoint" }
        httpClient.post("$endpoint?Action=SendMessage&MessageBody=${message}&Version=2012-11-05")
        log.debug { "Finished posting: <$message> to: $endpoint" }
    }
}