package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class Unsubscribe(private val subscriptionManager: SubscriptionManager) : Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        subscriptionManager.delete(params["SubscriptionArn"] ?: throw TODO())

        call.respondText {
            ObjectMapper.writeXmlElement(
                jdom("UnsubscribeResponse") {
                    awsMetadata()
                })
        }
    }
}