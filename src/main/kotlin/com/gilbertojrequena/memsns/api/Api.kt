package com.gilbertojrequena.memsns.api

import com.gilbertojrequena.memsns.api.action.RequestHandler
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun HttpStatusCode.isRetriable(): Boolean {
    return this.value > 499
}

fun Routing.api(requestHandler: RequestHandler) {
    snsApi(requestHandler)
}

private fun Routing.snsApi(requestHandler: RequestHandler) {
    route("") {
        get {
            requestHandler.processRequest(call)
        }
        post {
            requestHandler.processRequest(call)
        }
    }
}