package com.gilbertojrequena.memsns.api.action

import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters

interface Action {
    suspend fun execute(call: ApplicationCall, params: Parameters)
}