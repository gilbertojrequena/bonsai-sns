package com.gilbertojrequena.memsns.core

import com.gilbertojrequena.memsns.api.isRetriable
import com.gilbertojrequena.memsns.core.exception.HttpCallRetriableException
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import kotlinx.coroutines.delay
import mu.KotlinLogging

class SnsHttpClient {
    companion object {
        private const val RETRY_DELAY = 1000L
    }

    private val log = KotlinLogging.logger { }
    private val client = HttpClient(Apache) {
        followRedirects = false
    }

    suspend fun post(
        url: String,
        message: String = ""
    ) {
        var retries = 0
        var immediateRetries = 0
        while (immediateRetries < 3 || retries < 3) {
            try {
                invoke(url, message)
                return
            } catch (ex: Exception) {
                if (immediateRetries >= 3) {
                    log.warn { "Error executing HTTP call to $url, retrying in $RETRY_DELAY milliseconds" }
                    delay(RETRY_DELAY)
                    retries++
                } else {
                    log.warn { "Error executing HTTP call to $url, executing immediate retry" }
                    immediateRetries++
                }
            }
        }
        log.error { "Stopping HTTP calls to $url, no more retries left" }
    }

    private suspend fun invoke(
        url: String,
        message: String = ""
    ) {
        val response =
            client.post<HttpResponse>(url) {
                body = message
            }
        log.debug { "Http(s) call for <$url> response status is ${response.status}" }

        if (response.status.isRetriable()) {
            throw HttpCallRetriableException()
        }
    }
}