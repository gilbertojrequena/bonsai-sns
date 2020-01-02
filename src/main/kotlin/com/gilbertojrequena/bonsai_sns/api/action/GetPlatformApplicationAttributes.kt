package com.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.ObjectMapper
import com.gilbertojrequena.bonsai_sns.api.awsMetadata
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class GetPlatformApplicationAttributes : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("GetPlatformApplicationAttributesResponse") {
                    element("GetPlatformApplicationAttributesResult") {
                        element("Attributes") {
                        }
                    }
                    awsMetadata()
                })
        }
    }
}