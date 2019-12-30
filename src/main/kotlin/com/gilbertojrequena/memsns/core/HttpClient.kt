package com.gilbertojrequena.memsns.core

import com.gilbertojrequena.memsns.core.exception.UnsuccessfulHttpCallException
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import mu.KotlinLogging

internal class RetriableHttpClient {
    companion object {
        private const val RETRY_DELAY = 1000L
    }

    private val log = KotlinLogging.logger { }
    private val client = HttpClient(Apache) {
        followRedirects = false
    }

    suspend fun post(url: String, body: String = "") {
        log.debug { "Executing HTTP/S post, endpoint: '$url', message: '$body'" }
        var retries = 0
        var immediateRetries = 0
        while (immediateRetries < 3 || retries < 3) {
            val response = invoke(url, body)
            if (response.status.isSuccess()) {
                log.debug { "HTTP/S post executed successfully, endpoint: '$url', message: '$body'" }
                return
            } else if (response.status.isRetriable()) {
                if (immediateRetries >= 3) {
                    log.debug { "Error executing HTTP/S post, retrying in $RETRY_DELAY milliseconds, endpoint: '$url', message: '$body'" }
                    delay(RETRY_DELAY)
                    retries++
                } else {
                    log.debug { "Error executing HTTP/S post, executing immediate retry, endpoint: '$url', message: '$body'" }
                    immediateRetries++
                }
            } else {
                log.debug { "HTTP/S post executed and received response status '${response.status.value}', endpoint: '$url', message: '$body'" }
                throw UnsuccessfulHttpCallException(url, body)
            }
        }
        log.debug { "Stopping HTTP/S post, no more retries left, endpoint: '$url', message: '$body'" }
        throw UnsuccessfulHttpCallException(url, body)
    }

    private suspend fun invoke(url: String, body: String = ""): HttpResponse {
        val response = client.post<HttpResponse>(url) {
            this.body = body
        }
        log.debug { "Http(s) call for <$url> response status is ${response.status}" }
        return response
    }
}

private fun HttpStatusCode.isRetriable(): Boolean {
    return this.value > 499
}