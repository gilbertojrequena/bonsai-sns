package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.core.Config
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class CreatePlatformApplication(private val config: Config) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("CreatePlatformApplicationResponse") {
                    element("CreatePlatformApplicationResult") {
                        element("PlatformApplicationArn") {
                            text("arn:aws:sns:${config.region}:${config.accountId}:app/this-is-actually-not-real")
                        }
                    }
                    awsMetadata()
                })
        }
    }
}