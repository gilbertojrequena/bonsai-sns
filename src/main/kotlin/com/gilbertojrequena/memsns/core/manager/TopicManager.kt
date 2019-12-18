package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.Token
import com.gilbertojrequena.memsns.core.Topic
import com.gilbertojrequena.memsns.core.TopicArn
import com.gilbertojrequena.memsns.core.TopicsAndToken
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.SendChannel

class TopicManager(snsOpActor: SendChannel<SnsOpsMessage>) : SqsOperationsManager(snsOpActor) {

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

    private fun buildArn(topic: Topic) = "arn:aws:sns:memsns-region:123456789:${topic.name}"
}