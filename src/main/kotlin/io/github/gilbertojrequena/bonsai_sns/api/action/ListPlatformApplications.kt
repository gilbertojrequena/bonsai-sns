package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.ObjectMapper
import io.github.gilbertojrequena.bonsai_sns.api.awsMetadata
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class ListPlatformApplications : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("ListPlatformApplicationsResponse") {
                    element("ListPlatformApplicationsResult") {
                        element("PlatformApplications") {
                        }
                    }
                    awsMetadata()
                })
        }
    }
}