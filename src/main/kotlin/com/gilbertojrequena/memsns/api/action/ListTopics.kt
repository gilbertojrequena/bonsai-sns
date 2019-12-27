package com.gilbertojrequena.memsns.api.action

import com.gilbertojrequena.memsns.api.ObjectMapper.writeXmlElement
import com.gilbertojrequena.memsns.api.awsMetadata
import com.gilbertojrequena.memsns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText
import org.jonnyzzz.kotlin.xml.dsl.jdom.jdom

internal class ListTopics(private val topicManager: TopicManager) :
    Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val topicsAndToken = topicManager.findAll(params["NextToken"])

        call.respondText {
            writeXmlElement(jdom("ListTopicsResponse") {
                element("ListTopicsResult") {
                    if (topicsAndToken.nextToken != null) {
                        element("NextToken") {
                            text(topicsAndToken.nextToken)
                        }
                    }
                    element("Topics") {
                        for (topic in topicsAndToken.topics) {
                            element("member") {
                                element("TopicArn") {
                                    text(topic.arn)
                                }
                            }
                        }
                    }
                }
                awsMetadata()
            })
        }
    }
}

