package io.github.gilbertojrequena.bonsai_sns.api.action

import io.github.gilbertojrequena.bonsai_sns.api.xml
import io.github.gilbertojrequena.bonsai_sns.core.manager.TopicManager
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.response.respondText

internal class ListTopics(private val topicManager: TopicManager) :
    Action {
    override suspend fun execute(call: ApplicationCall, params: Parameters) {
        val topicsAndToken = topicManager.findAll(params["NextToken"])

        call.respondText {
            xml("ListTopicsResponse") {
                element("ListTopicsResult") {
                    if (topicsAndToken.nextToken != null) {
                        element("NextToken") {
                            text = topicsAndToken.nextToken
                        }
                    }
                    element("Topics") {
                        for (topic in topicsAndToken.topics) {
                            element("member") {
                                element("TopicArn") {
                                    text = topic.arn
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

