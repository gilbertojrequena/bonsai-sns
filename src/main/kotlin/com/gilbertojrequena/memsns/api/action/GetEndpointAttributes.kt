package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class GetEndpointAttributes : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("GetEndpointAttributesResponse") {
                    element("GetEndpointAttributesResult") {
                        element("Attributes") {
                        }
                    }
                    awsMetadata()
                })
        }
    }
}