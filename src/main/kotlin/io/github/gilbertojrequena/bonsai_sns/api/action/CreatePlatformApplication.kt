package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.server.BonsaiSnsConfig
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText

internal class CreatePlatformApplication(private val config: BonsaiSnsConfig) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            xml("CreatePlatformApplicationResponse") {
                element("CreatePlatformApplicationResult") {
                    element("PlatformApplicationArn") {
                        text = "arn:aws:sns:${config.region}:${config.accountId}:app/this-is-actually-not-real"
                    }
                }
            }
        }
    }
}