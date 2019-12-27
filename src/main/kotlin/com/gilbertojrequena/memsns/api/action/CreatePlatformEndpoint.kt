package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.server.MemSnsConfig
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class CreatePlatformEndpoint(private val config: MemSnsConfig) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("CreatePlatformEndpointResponse") {
                    element("CreatePlatformEndpointResult") {
                        text("arn:aws:sns:${config.region}:${config.accountId}:endpoint/this-is-actually-not-real")
                    }
                    awsMetadata()
                })
        }
    }
}