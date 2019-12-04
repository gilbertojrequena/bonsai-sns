package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.Token
import com.gilbertojrequena.memsns.core.Topic
import com.gilbertojrequena.memsns.core.TopicArn
import com.gilbertojrequena.memsns.core.TopicsAndToken
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

class TopicManager(private val snsOpActor: SendChannel<SnsOpsMessage>) {

    suspend fun create(topic: Topic): Topic {
        val responseChannel = Channel<Topic>()
        snsOpActor.send(
            SnsOpsMessage.SaveTopic(
                Topic(
                    topic.name,
                    topic.displayName,
                    topic.deliveryRetry,
                    buildArn(topic),
                    topic.attributes,
                    topic.tags
                ), responseChannel
            )
        )
        return responseChannel.receive()
    }

    suspend fun findAll(fromToken: Token?): TopicsAndToken {
        val responseChannel = Channel<TopicsAndToken>()
        snsOpActor.send(SnsOpsMessage.FindAllTopics(fromToken, responseChannel))
        return responseChannel.receive()
    }

    suspend fun findByArn(arn: TopicArn): Topic? {
        val responseChannel = Channel<Topic?>()
        snsOpActor.send(SnsOpsMessage.FindTopicByArn(arn, responseChannel))
        return responseChannel.receive() ?: throw TODO("Implement exception for topic not found")
    }

    suspend fun exists(arn: TopicArn): Boolean {
        val responseChannel = Channel<Boolean>()
        snsOpActor.send(SnsOpsMessage.TopicExists(arn, responseChannel))
        return responseChannel.receive()
    }

    suspend fun delete(arn: TopicArn): Boolean {
        val responseChannel = Channel<Boolean>()
        snsOpActor.send(SnsOpsMessage.DeleteTopic(arn, responseChannel))
        return responseChannel.receive()
    }

    private fun buildArn(topic: Topic) = "arn:memsns:sns:memsns-region:123456789:${topic.name}"
}