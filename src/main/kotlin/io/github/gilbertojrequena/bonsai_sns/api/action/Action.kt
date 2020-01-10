package io.github.gilbertojrequena.bonsai_sns.api.action

import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters

internal interface Action {
    suspend fun execute(call: ApplicationCall, params: Parameters)
}