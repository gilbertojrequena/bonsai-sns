package com.gilbertojrequena.memsns.core

import com.gilbertojrequena.memsns.api.isRetriable
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.post
import io.ktor.client.response.HttpResponse
import kotlinx.coroutines.delay
import mu.KotlinLogging

//    TODO Retry logic
//    https://docs.aws.amazon.com/sns/latest/dg/sns-message-delivery-retries.html
class SnsHttpClient {

    //    TODO Try creating an actor for this
    private val log = KotlinLogging.logger { }
    private val client = HttpClient(Apache) {
        followRedirects = true
    }

    suspend fun post(
        url: String,
        message: String = "",
        deliveryRetry: Topic.DeliveryRetry = Topic.DeliveryRetry()
    ) {
        var retries = 0

        while (retries < deliveryRetry.numberOfRetries) {
            try {
                invoke(url, message)
                return
            } catch (ex: Exception) {
                log.warn { "Error executing HTTP call to $url, retrying in ${deliveryRetry.maximumDelay} seconds" }
                delay(deliveryRetry.maximumDelay * 1000L)
                retries++
            }
        }
        log.warn { "Stopping HTTP calls to $url, no more retries left" }
        //todo throw exception
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
            throw RuntimeException("") // TODO
        }
    }
}