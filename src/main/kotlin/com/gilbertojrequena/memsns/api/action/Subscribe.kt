package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.api.createTopicSubscriptionData
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

class Subscribe(private val subscriptionManager: SubscriptionManager) :
    Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val subscription = subscriptionManager.create(params.createTopicSubscriptionData())

        call.respondText {
            writeXmlElement(
                jdom("SubscribeResponse") {
                    element("SubscribeResult") {
                        element("SubscriptionArn") {
                            text(subscription.arn)
                        }
                    }
                    awsMetadata()
                })
        }
    }
}

