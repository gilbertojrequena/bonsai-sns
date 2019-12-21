package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.*
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.SendChannel

class TopicManager(snsOpActor: SendChannel<SnsOpsMessage>, private val config: Config) :
    SqsOperationsManager(snsOpActor) {

    suspend fun create(topic: Topic): Topic {
        return sendToActorAndReceive {
            SnsOpsMessage.SaveTopic(
                Topic(
                    topic.name,
                    buildArn(topic),
                    topic.attributes,
                    topic.tags
                ), it
            )
        }
    }

    suspend fun findAll(fromToken: Token? = null): TopicsAndToken {
        return sendToActorAndReceive { SnsOpsMessage.FindAllTopics(fromToken, it) }
    }

    suspend fun findByArn(arn: TopicArn): Topic {
        return sendToActorAndReceive { SnsOpsMessage.FindTopicByArn(arn, it) }
    }

    suspend fun exists(arn: TopicArn): Boolean {
        return sendToActorAndReceive { SnsOpsMessage.TopicExists(arn, it) }
    }

    suspend fun delete(arn: TopicArn): Topic {
        return sendToActorAndReceive { SnsOpsMessage.DeleteTopic(arn, it) }
    }

    private fun buildArn(topic: Topic) = "arn:aws:sns:${config.region}:${config.accountId}:${topic.name}"
}