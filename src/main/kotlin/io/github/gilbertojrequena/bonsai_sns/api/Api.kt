package io.github.gilbertojrequena.bonsai_sns.api

import io.github.gilbertojrequena.bonsai_sns.api.action.RequestHandler
import io.github.gilbertojrequena.bonsai_sns.api.exception.InvalidParameterException
import io.github.gilbertojrequena.bonsai_sns.api.exception.MessageAttributeValidationException
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal fun Routing.api(requestHandler: RequestHandler) {
    snsApi(requestHandler)
}

private fun Routing.snsApi(requestHandler: RequestHandler) {
    route("") {
        get {
            requestHandler.processRequest(call)
        }
        post {
            requestHandler.processRequest(call)
        }
    }
}

internal inline fun <reified T> StatusPages.Configuration.invalidParameter() {
    exception<InvalidParameterException> { cause ->
        call.respondText(contentType = ContentType.Application.Xml, status = HttpStatusCode.BadRequest) {
            ObjectMapper.writeXmlElement(
                jdom("ErrorResponse") {
                    element("Error") {
                        element("Type") {
                            text("Sender")
                        }
                        element("Code") {
                            text("InvalidParameter")
                        }
                        element("Message") {
                            text("InvalidParameter: ${cause.reason}")
                        }
                    }
                    awsMetadata()
                })
        }
    }
}

internal inline fun <reified T> StatusPages.Configuration.parameterValueInvalid() {
    exception<MessageAttributeValidationException> { cause ->
        call.respondText(contentType = ContentType.Application.Xml, status = HttpStatusCode.BadRequest) {
            ObjectMapper.writeXmlElement(
                jdom("ErrorResponse") {
                    element("Error") {
                        element("Type") {
                            text("Sender")
                        }
                        element("Code") {
                            text("ParameterValueInvalid")
                        }
                        element("Message") {
                            text("${cause.message}")
                        }
                    }
                    awsMetadata()
                })
        }
    }
}