package io.github.gilbertojrequena.bonsai_sns.core.actor.dispatcher

import io.github.gilbertojrequena.bonsai_sns.core.RetriableHttpClient
import io.github.gilbertojrequena.bonsai_sns.core.exception.MessageDispatchException
import mu.KotlinLogging

internal class HttpMessageDispatcher(
    private val httpClient: RetriableHttpClient,
    private val messageBodyFactory: MessageBodyFactory
) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun dispatch(dispatchMessageRequest: DispatchMessageRequest) {
        val endpoint = dispatchMessageRequest.endpoint
        val messageBody = messageBodyFactory.create(dispatchMessageRequest)
        log.debug { "Posting: '$messageBody' to endpoint: '$endpoint'" }
        try {
            httpClient.post(endpoint, messageBody)
        } catch (e: Exception) {
            throw MessageDispatchException(endpoint, messageBody)
        }
        log.debug { "Finished posting: '$messageBody' to endpoint: '$messageBody'" }
    }
}
