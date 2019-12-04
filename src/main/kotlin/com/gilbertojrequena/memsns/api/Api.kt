package com.gilbertojrequena.memsns.api

import com.gilbertojrequena.memsns.api.action.RequestHandler
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import java.util.*

fun HttpStatusCode.isRetriable(): Boolean {
    return this.value > 499
}

fun Routing.api(requestHandler: RequestHandler) {
    snsApi(requestHandler)
}

private fun Routing.snsApi(requestHandler: RequestHandler) {
    val random = Random(523523L)
    route("") {
        get {
            requestHandler.processRequest(call)
        }
        post {
            requestHandler.processRequest(call)
        }
    }
    route("/action") {
        post {
            val log = KotlinLogging.logger { }
            log.debug { "Message for action received" }
            if (random.nextInt(5) == 4) {
                call.respond(HttpStatusCode.ServiceUnavailable, "")
            } else {
                call.respond(HttpStatusCode.OK, "")
            }
        }
    }
}