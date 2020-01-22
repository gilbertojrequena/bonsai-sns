package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText

internal class RemovePermission : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            xml("RemovePermissionResponse")
        }
    }
}