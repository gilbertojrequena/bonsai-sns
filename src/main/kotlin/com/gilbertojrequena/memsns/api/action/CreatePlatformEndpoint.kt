package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class CreatePlatformEndpoint : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("CreatePlatformEndpointResponse") {
                    element("CreatePlatformEndpointResult") {
                        text("arn:memsns:sns:sns:memsns-region:123456789012:endpoint/this-is-actually-not-real")
                    }
                    awsMetadata()
                })
        }
    }
}