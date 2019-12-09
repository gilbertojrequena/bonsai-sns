package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.Topic
import com.gilbertojrequena.memsns.core.actor.snsOpsActor
import com.gilbertojrequena.memsns.core.exception.TopicAlreadyExistException
import com.gilbertojrequena.memsns.core.exception.TopicNotFoundException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows


@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class TopicManagerTest {

    private val topicManager = TopicManager(snsOpsActor())

    @Test
    fun `should throw exception when topic is not found`() {
        assertThrows<TopicNotFoundException> {
            runBlocking {
                topicManager.findByArn("non-existent")
            }
        }
    }

    @Test
    fun `should return empty list when there are no topics`() {
        runBlocking {
            val allTopics = topicManager.findAll()
            assertEquals(0, allTopics.topics.size)
        }
    }

    @Test
    fun `should create topic`() {
        runBlocking {
            val topic = topicManager.create(Topic("test-topic", "display-name"))

            assertNotNull(topic)
            assertEquals("test-topic", topic.name)
            assertEquals("display-name", topic.displayName)
            assertEquals("arn:memsns:sns:memsns-region:123456789:${topic.name}", topic.arn)

            val t = topicManager.findByArn(topic.arn)
            assertEquals(topic, t)
        }
    }

    @Test
    fun `should throw exception when trying to create existent topic`() {
        runBlocking {
            topicManager.create(Topic("test-topic", "display-name"))

            assertThrows<TopicAlreadyExistException> {
                runBlocking {
                    topicManager.create(Topic("test-topic", "display-name"))
                }
            }
        }
    }

    @Test
    fun `should find all topics without token when there are less than 101`() {
        runBlocking {
            repeat(5) {
                topicManager.create(Topic("test-topic-$it", "display-name-$it"))
            }
            val topicsAndToken = topicManager.findAll()
            val token = topicsAndToken.nextToken
            val topics = topicsAndToken.topics

            assertNull(token)
            assertEquals(5, topics.size)
        }
    }

    @Test
    fun `should find all topics with token when there are more than 100`() {
        runBlocking {
            repeat(120) {
                topicManager.create(Topic("test-topic-$it", "display-name-$it"))
            }
            val hundredTopicsAndToken = topicManager.findAll()

            assertNotNull(hundredTopicsAndToken.nextToken)
            assertEquals(100, hundredTopicsAndToken.topics.size)

            val twentyTopicsAndToken = topicManager.findAll(hundredTopicsAndToken.nextToken)

            assertNull(twentyTopicsAndToken.nextToken)
            assertEquals(20, twentyTopicsAndToken.topics.size)
        }
    }

    @Test
    fun `should return false when topic doesn't exist`() {
        runBlocking {
            assertFalse(topicManager.exists("arn"))
        }
    }

    @Test
    fun `should return true when topic exist`() {
        runBlocking {
            val topic = topicManager.create(Topic("test-topic", "display-name"))

            assertTrue(topicManager.exists(topic.arn))
        }
    }

    @Test
    fun `should throw exception when trying to delete topic which doesn't exist`() {
        assertThrows<TopicNotFoundException> {
            runBlocking {
                topicManager.delete("non-existent")
            }
        }
    }

    @Test
    fun `should delete topic`() {
        runBlocking {
            val topic = topicManager.create(Topic("test-topic", "display-name"))

            val deletedTopic = topicManager.delete(topic.arn)
            assertEquals(topic, deletedTopic)
        }
    }
}