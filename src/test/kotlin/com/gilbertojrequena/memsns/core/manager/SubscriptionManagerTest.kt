package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.Subscription.Protocol.HTTP
import com.gilbertojrequena.memsns.core.Topic
import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import com.gilbertojrequena.memsns.core.actor.snsOpsActor
import com.gilbertojrequena.memsns.core.exception.SubscriptionAlreadyExist
import com.gilbertojrequena.memsns.core.exception.TopicNotFoundException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class SubscriptionManagerTest {

    private val snsOpActor = snsOpsActor()
    private val subscriptionManager = SubscriptionManager(snsOpActor)

    @BeforeEach
    fun setUp() {
        runBlocking {
            val topicResponse = Channel<Topic>()
            snsOpActor.send(
                SnsOpsMessage.SaveTopic(
                    Topic(
                        name = "test-topic",
                        arn = "test-topic-arn"
                    ), topicResponse
                )
            )
            topicResponse.receive()
        }
    }

    @Test
    fun `should throw exception when subscription topic is not found`() {
        assertThrows<TopicNotFoundException> {
            runBlocking {
                subscriptionManager.create(Subscription("topic-arn", HTTP, "endpoint"))
            }
        }
    }

    @Test
    fun `should subscribe to topic`() {
        runBlocking {
            val subscription = subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint"))
            assertEquals("test-topic-arn", subscription.topicArn)
//                assertEquals("arn:memsns:sns:memsns-region:123456789:test-topic:", subscription.arn)
            assertEquals(HTTP, subscription.protocol)
            assertEquals("endpoint", subscription.endpoint)
            assertEquals("owner", subscription.owner)
        }
    }

    @Test
    fun `should throw exception when trying to subscribe to the same topic with the same protocol and endpoint`() {
        assertThrows<SubscriptionAlreadyExist> {
            runBlocking {
                repeat(2) {
                    subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint"))
                }
            }
        }
    }

    @Test
    fun `should find all subscription without token when there are less than 101`() {
        runBlocking {
            repeat(5) {
                subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint-$it"))
            }
            val subscriptionsAndToken = subscriptionManager.findAll()
            val token = subscriptionsAndToken.nextToken
            val subscriptions = subscriptionsAndToken.subscriptions

            assertNull(token)
            assertEquals(5, subscriptions.size)
        }
    }

    @Test
    fun `should find all subscription with token when there are more than 100`() {
        runBlocking {
            repeat(120) {
                subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint-$it"))
            }
            val hundredSubscriptionsAndToken = subscriptionManager.findAll()
            val hundredSubscriptionsToken = hundredSubscriptionsAndToken.nextToken
            val hundredSubscriptions = hundredSubscriptionsAndToken.subscriptions

            assertNotNull(hundredSubscriptionsToken)
            assertEquals(100, hundredSubscriptions.size)

            val twentySubscriptionsAndToken = subscriptionManager.findAll(hundredSubscriptionsToken)
            val twentySubscriptionsToken = twentySubscriptionsAndToken.nextToken
            val twentySubscriptions = twentySubscriptionsAndToken.subscriptions

            assertNull(twentySubscriptionsToken)
            assertEquals(20, twentySubscriptions.size)
        }
    }

    @Test
    fun `should throw exception when trying to find subscriptions for topic which doesn't exist`() {
        assertThrows<TopicNotFoundException> {
            runBlocking {
                subscriptionManager.findAllByTopicArn("arn")
            }
        }
    }

    @Test
    fun `should find subscriptions by topic with no token when there are less than 101`() {
        runBlocking {
            repeat(5) {
                subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint-$it"))
            }
            val subscriptionsAndToken = subscriptionManager.findAllByTopicArn("test-topic-arn")

            assertNull(subscriptionsAndToken.nextToken)
            assertEquals(5, subscriptionsAndToken.subscriptions.size)
        }
    }

    @Test
    fun `should fin subscriptions by topic with token when there are more than 100`() {
        runBlocking {
            repeat(120) {
                subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint-$it"))
            }
            val hundredSubscriptionsAndToken = subscriptionManager.findAllByTopicArn("test-topic-arn")
            val hundredSubscriptionsToken = hundredSubscriptionsAndToken.nextToken
            val hundredSubscriptions = hundredSubscriptionsAndToken.subscriptions

            assertNotNull(hundredSubscriptionsToken)
            assertEquals(100, hundredSubscriptions.size)

            val twentySubscriptionsAndToken =
                subscriptionManager.findAllByTopicArn("test-topic-arn", hundredSubscriptionsToken)
            val twentySubscriptionsToken = twentySubscriptionsAndToken.nextToken
            val twentySubscriptions = twentySubscriptionsAndToken.subscriptions

            assertNull(twentySubscriptionsToken)
            assertEquals(20, twentySubscriptions.size)
        }
    }

    @Test
    fun `should not throw exception when trying to delete non existent subscription`() {
        assertDoesNotThrow {
            runBlocking {
                subscriptionManager.delete("nope")
            }
        }
    }

    @Test
    fun `should delete subscription`() {
        runBlocking {
            val subscription = subscriptionManager.create(Subscription("test-topic-arn", HTTP, "endpoint"))

            subscriptionManager.delete(subscription.arn)

            assertEquals(0, subscriptionManager.findAll().subscriptions.size)
        }
    }
}