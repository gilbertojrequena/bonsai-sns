package com.gilbertojrequena.bonsai_sns.api

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder
import com.amazonaws.services.sns.model.*
import com.amazonaws.services.sns.model.Tag
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.gilbertojrequena.bonsai_sns.server.BonsaiSnsServer
import io.ktor.application.call
import io.ktor.request.receiveText
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.elasticmq.rest.sqs.SQSRestServer
import org.elasticmq.rest.sqs.SQSRestServerBuilder
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.TimeUnit

internal class AwsSnsInterfaceTest {

    companion object {
        private lateinit var snsClient: AmazonSNS
        private lateinit var sqsClient: AmazonSQSAsync
        private lateinit var server: BonsaiSnsServer
        private lateinit var sQSRestServer: SQSRestServer
        private lateinit var testTopicArn: String
        private lateinit var testQueueUrl: String
        private lateinit var testQueueArn: String
        private lateinit var testDlqUrl: String
        private lateinit var testDlqArn: String
        private lateinit var httpServer: ApplicationEngine
        private val httpMessagesChannel = Channel<String>()
        private val basicAWSCredentials = BasicAWSCredentials("foo", "bar")
        private const val region = "region"
        private const val port = 7979
        private const val accountId = 123456789L

        @JvmStatic
        @BeforeAll
        fun setUp() {
            server = BonsaiSnsServer.builder()
                .withRegion(region)
                .withAccountId(accountId)
                .withPort(port)
                .withSqsEndpoint("http://localhost:9325")
                .start()
            sQSRestServer = SQSRestServerBuilder.withPort(9325).withInterface("localhost").start()
            sqsClient = AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration("http://localhost:9325", region)
                )
                .build()
            snsClient = AmazonSNSAsyncClientBuilder.standard()
                .withCredentials(AWSStaticCredentialsProvider(basicAWSCredentials))
                .withEndpointConfiguration(
                    AwsClientBuilder.EndpointConfiguration(
                        "http://localhost:$port",
                        region
                    )
                )
                .build()

            val env = applicationEngineEnvironment {
                module {
                    routing {
                        route("/target") {
                            post {
                                httpMessagesChannel.send(call.receiveText())
                            }
                        }
                    }
                }
                connector {
                    host = "0.0.0.0"
                    port = 7777
                }
            }
            httpServer = embeddedServer(Netty, env).start()
        }

        @JvmStatic
        @AfterAll
        fun shutDown() {
            server.stop()
            sQSRestServer.stopAndWait()
            httpServer.stop(0, 1, TimeUnit.MILLISECONDS)
        }
    }

    @BeforeEach
    fun beforeEach() {
        testTopicArn = snsClient.createTopic("test-topic").topicArn
        testQueueUrl = sqsClient.createQueue("test-queue").queueUrl
        testQueueArn = sqsClient.getQueueAttributes(
            GetQueueAttributesRequest(testQueueUrl)
                .withAttributeNames("QueueArn")
        ).attributes["QueueArn"]!!

        testDlqUrl = sqsClient.createQueue("test-dlq").queueUrl
        testDlqArn = sqsClient.getQueueAttributes(
            GetQueueAttributesRequest(testDlqUrl)
                .withAttributeNames("QueueArn")
        ).attributes["QueueArn"]!!
    }

    @AfterEach
    fun afterEach() {
        snsClient.listSubscriptions().subscriptions.forEach { snsClient.unsubscribe(it.subscriptionArn) }
        snsClient.listTopics().topics.forEach { snsClient.deleteTopic(it.topicArn) }
        sqsClient.listQueues().queueUrls.forEach { sqsClient.deleteQueue(it) }
    }

    @Test
    fun `should create topic`() {
        assertEquals(
            "arn:aws:sns:$region:$accountId:test-topic",
            testTopicArn
        )

        val listTopicsResult = snsClient.listTopics()
        assertNotNull(listTopicsResult.topics.find { it.topicArn == testTopicArn })
    }

    @Test
    fun `should create subscription`() {
        val subscriptionResults = listOf(
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777"),
            snsClient.subscribe(testTopicArn, "https", "https://localhost:7777"),
            snsClient.subscribe(testTopicArn, "sqs", testQueueArn)
        )
        val listSubscriptionsResult = snsClient.listSubscriptions()
        for (subscriptionResult in subscriptionResults) {
            assertNotNull(listSubscriptionsResult.subscriptions.find { it.subscriptionArn == subscriptionResult.subscriptionArn })
        }
    }

    @Test
    fun `should list subscription by topic`() {
        snsClient.createTopic("some-other-topic")
        val subscriptionResults = listOf(
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777"),
            snsClient.subscribe(testTopicArn, "https", "https://localhost:7777"),
            snsClient.subscribe(testTopicArn, "sqs", testQueueArn)
        )
        val listSubscriptionsResult = snsClient.listSubscriptionsByTopic(testTopicArn)
        for (subscriptionResult in subscriptionResults) {
            assertNotNull(listSubscriptionsResult.subscriptions.find { it.subscriptionArn == subscriptionResult.subscriptionArn })
        }
    }

    @Test
    fun `should add subscription attributes`() {
        val subscriptionResult = snsClient.subscribe(testTopicArn, "http", "http://localhost:7777")
        snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")

        val subscriptionAttributesResult = snsClient.getSubscriptionAttributes(subscriptionResult.subscriptionArn)

        assertEquals("true", subscriptionAttributesResult.attributes["RawMessageDelivery"])
    }

    @Test
    fun `should unsubscribe`() {
        val subscription = snsClient.subscribe(testTopicArn, "http", "http://localhost:7777")

        snsClient.unsubscribe(UnsubscribeRequest(subscription.subscriptionArn))

        val subscriptions = snsClient.listSubscriptions()
        assertTrue(subscriptions.subscriptions.isEmpty())
    }

    @Test
    fun `should delete topic`() {
        snsClient.deleteTopic(
            testTopicArn
        )

        val listTopicsResult = snsClient.listTopics()
        assertTrue(listTopicsResult.topics.isEmpty())
    }

    @Test
    fun `should publish http message`() = runBlocking {
        snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target")
        snsClient.publish(
            PublishRequest(
                testTopicArn, "message"
            )
        )
        val message = httpMessagesChannel.receive()
        assertTrue(message.matches(Regex("\\{\"Type\":\"Notification\",\"MessageId\":\"[a-zA-Z0-9-]*\",\"TopicArn\":\"arn:aws:sns:$region:$accountId:test-topic\",\"Message\":\"message\",\"SignatureVersion\":\"1\",\"Signature\":\"[a-zA-Z0-9 /+-=]*\",\"SigningCertURL\":\"https%3A%2F%2Flocalhost%3A$port%2FSimpleNotificationService-6aad65c2f9911b05cd53efda11f913f9.pem\",\"UnsubscribeURL\":\"http%3A%2F%2Flocalhost%3A$port%2F%3FAction%3DUnsubscribe%26SubscriptionArn%3Darn%3Aaws%3Asns%3A$region%3A$accountId%3Atest-topic%3A[a-zA-Z0-9]*-[a-zA-Z0-9]*-[a-zA-Z0-9]*\"}")))
    }

    @Test
    fun `should publish raw message to http endpoint`() = runBlocking {
        val subscriptionResult = snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target")
        snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")
        snsClient.publish(
            PublishRequest(
                testTopicArn, "message"
            )
        )
        val message = httpMessagesChannel.receive()
        assertEquals("message", message)
    }

    @Test
    fun `should filter message with no attributes`() = runBlocking {
        val subscriptionResult = snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target")
        snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")
        snsClient.setSubscriptionAttributes(
            SetSubscriptionAttributesRequest()
                .withAttributeName("FilterPolicy")
                .withAttributeValue("""{"a": [{"numeric": [">", 0] }]}""")
                .withAttributeValue("""{"b": [{"anything-but": ["rugby", "tennis"]}]}""")
                .withSubscriptionArn(subscriptionResult.subscriptionArn)
        )
        snsClient.publish(PublishRequest(testTopicArn, "message"))
        try {
            withTimeout(50L) {
                httpMessagesChannel.receive()
                throw RuntimeException("Message was received")
            }
        } catch (e: TimeoutCancellationException) {
        }
    }

    @Test
    fun `should filter message because of filter policy`() = runBlocking {
        val subscriptionResult = snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target")
        snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")
        snsClient.setSubscriptionAttributes(
            SetSubscriptionAttributesRequest()
                .withAttributeName("FilterPolicy")
                .withAttributeValue("""{"a": [{"numeric": [">", 0] }]}""")
                .withAttributeValue("""{"b": [{"anything-but": ["rugby", "tennis"]}]}""")
                .withSubscriptionArn(subscriptionResult.subscriptionArn)
        )
        snsClient.publish(
            PublishRequest(testTopicArn, "message").withMessageAttributes(
                mapOf<String, MessageAttributeValue>(
                    "a" to MessageAttributeValue().withDataType("Number").withStringValue("-1")
                )
            )
        )
        try {
            withTimeout(50L) {
                httpMessagesChannel.receive()
                throw RuntimeException("Message was received")
            }
        } catch (e: TimeoutCancellationException) {
        }
    }

    @Test
    fun `should accept message because of filter policy`() = runBlocking {
        val subscriptionResult = snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target")
        snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")
        snsClient.setSubscriptionAttributes(
            SetSubscriptionAttributesRequest()
                .withAttributeName("FilterPolicy")
                .withAttributeValue("""{"a": [{"numeric": [">", 0] }]}""")
                .withAttributeValue("""{"b": [{"anything-but": ["rugby", "tennis"]}]}""")
                .withSubscriptionArn(subscriptionResult.subscriptionArn)
        )
        snsClient.publish(
            PublishRequest(testTopicArn, "message").withMessageAttributes(
                mapOf<String, MessageAttributeValue>(
                    "a" to MessageAttributeValue().withDataType("Number").withStringValue("10"),
                    "b" to MessageAttributeValue().withDataType("String").withStringValue("<3 dogs")
                )
            )
        )

        val message = httpMessagesChannel.receive()
        assertEquals("message", message)
    }

    @Test
    fun `should publish to sqs`() {
        snsClient.subscribe(testTopicArn, "sqs", testQueueArn)

        snsClient.publish(PublishRequest(testTopicArn, "message"))

        val message = getMessageFromQueue(testQueueUrl)

        assertNotNull(message)
    }

    @Test
    fun `should publish raw message to sqs`() {
        val subscriptionResult = snsClient.subscribe(testTopicArn, "sqs", testQueueArn)
        snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")
        snsClient.publish(
            PublishRequest(
                testTopicArn, "message"
            )
        )

        val message = getMessageFromQueue(testQueueUrl)
        assertEquals("message", message.body)
    }

    @Test
    fun `should subscribe once for the same subscription request`() = runBlocking {

        val subscriptionResults = listOf(
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target"),
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target"),
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target")
        )
        for (subscriptionResult in subscriptionResults) {
            snsClient.setSubscriptionAttributes(subscriptionResult.subscriptionArn, "RawMessageDelivery", "true")
        }
        snsClient.publish(PublishRequest(testTopicArn, "message"))

        httpMessagesChannel.receive()
        try {
            withTimeout(50L) {
                httpMessagesChannel.receive()
                throw RuntimeException("More than one message delivered")
            }
        } catch (e: TimeoutCancellationException) {
        }
    }

    @Test
    fun `should publish to multiple subscriptions`() = runBlocking {
        listOf(
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target"),
            snsClient.subscribe(testTopicArn, "sqs", testQueueArn)
        ).forEach {
            snsClient.setSubscriptionAttributes(it.subscriptionArn, "RawMessageDelivery", "true")
        }
        snsClient.publish(PublishRequest(testTopicArn, "message"))

        val httpMessage = httpMessagesChannel.receive()
        assertEquals("message", httpMessage)
        val message = getMessageFromQueue(testQueueUrl)
        assertEquals("message", message.body)
    }


    @Test
    fun `should send failed message to dead letter queue`() = runBlocking {
        listOf(
            snsClient.subscribe(testTopicArn, "http", "http://localhost:7777/target-nope"),
            snsClient.subscribe(testTopicArn, "sqs", "$testQueueArn-nope")
        ).forEach {
            snsClient.setSubscriptionAttributes(it.subscriptionArn, "RawMessageDelivery", "true")
            snsClient.setSubscriptionAttributes(
                SetSubscriptionAttributesRequest()
                    .withAttributeName("RedrivePolicy")
                    .withAttributeValue("""{"deadLetterTargetArn": "$testDlqArn"}""")
                    .withSubscriptionArn(it.subscriptionArn)
            )
        }
        snsClient.publish(PublishRequest(testTopicArn, "message"))

        repeat(2) {
            val message = getMessageFromQueue(testDlqUrl)
            assertEquals("message", message.body)
        }
    }

    @Test
    fun `should respond successfully to AddPermission`() {
        snsClient.addPermission(
            AddPermissionRequest(
                testTopicArn,
                "label",
                listOf(""),
                listOf("actions")
            )
        )
    }

    @Test
    fun `should respond successfully to CheckIfPhoneNumberIsOptedOut`() {
        snsClient.checkIfPhoneNumberIsOptedOut(
            CheckIfPhoneNumberIsOptedOutRequest().withPhoneNumber("555123456")
        )
    }

    @Test
    fun `should respond successfully to ConfirmSubscription`() {
        snsClient.confirmSubscription(testTopicArn, "token")
    }

    @Test
    fun `should respond successfully to CreatePlatformApplication`() {
        snsClient.createPlatformApplication(CreatePlatformApplicationRequest().withPlatform("platform"))
    }

    @Test
    fun `should respond successfully to DeletePlatformApplication`() {
        snsClient.deletePlatformApplication(DeletePlatformApplicationRequest().withPlatformApplicationArn("arn"))
    }

    @Test
    fun `should respond successfully to GetEndpointAttributes`() {
        snsClient.getEndpointAttributes(GetEndpointAttributesRequest().withEndpointArn("arn"))
    }

    @Test
    fun `should respond successfully to GetPlatformApplicationAttributes`() {
        snsClient.getPlatformApplicationAttributes(
            GetPlatformApplicationAttributesRequest().withPlatformApplicationArn("arn")
        )
    }

    @Test
    fun `should respond successfully to GetSMSAttributes`() {
        snsClient.getSMSAttributes(GetSMSAttributesRequest())
    }

    @Test
    fun `should respond successfully to GetTopicAttributes`() {
        snsClient.getTopicAttributes(GetTopicAttributesRequest(testTopicArn))
    }

    @Test
    fun `should respond successfully to ListEndpointsByPlatformApplication`() {
        snsClient.listEndpointsByPlatformApplication(
            ListEndpointsByPlatformApplicationRequest().withPlatformApplicationArn("arn")
        )
    }

    @Test
    fun `should respond successfully to ListPhoneNumbersOptedOut`() {
        snsClient.listPhoneNumbersOptedOut(ListPhoneNumbersOptedOutRequest())
    }

    @Test
    fun `should respond successfully to ListPlatformApplications`() {
        snsClient.listPlatformApplications(ListPlatformApplicationsRequest())
    }

    @Test
    fun `should respond successfully to ListTagsForResource`() {
        snsClient.listTagsForResource(ListTagsForResourceRequest().withResourceArn("arn"))
    }

    @Test
    fun `should respond successfully to OptInPhoneNumber`() {
        snsClient.optInPhoneNumber(OptInPhoneNumberRequest().withPhoneNumber("555123456"))
    }

    @Test
    fun `should respond successfully to RemovePermission`() {
        snsClient.removePermission(RemovePermissionRequest(testTopicArn, "label"))
    }

    @Test
    fun `should respond successfully to SetEndpointAttributes`() {
        snsClient.setEndpointAttributes(
            SetEndpointAttributesRequest()
                .withEndpointArn("arn")
                .withAttributes(
                    mapOf("key" to "value")
                )
        )
    }

    @Test
    fun `should respond successfully to SetPlatformApplicationAttributes`() {
        snsClient.setPlatformApplicationAttributes(
            SetPlatformApplicationAttributesRequest()
                .withPlatformApplicationArn("arn")
                .withAttributes(
                    mapOf("key" to "value")
                )
        )
    }

    @Test
    fun `should respond successfully to SetSMSAttributes`() {
        snsClient.setSMSAttributes(SetSMSAttributesRequest().withAttributes(mapOf("key" to "value")))
    }

    @Test
    fun `should respond successfully to SetTopicAttributes`() {
        snsClient.setTopicAttributes(SetTopicAttributesRequest(testTopicArn, "key", "value"))
    }

    @Test
    fun `should respond successfully to TagResource`() {
        snsClient.tagResource(
            TagResourceRequest().withResourceArn(testTopicArn).withTags(
                Tag().withKey("key").withValue("value")
            )
        )
    }

    @Test
    fun `should respond successfully to UntagResource`() {
        snsClient.untagResource(
            UntagResourceRequest().withResourceArn(
                testTopicArn
            )
        )
    }

    private fun getMessageFromQueue(queueUrl: String): Message {
        return sqsClient.receiveMessage(
            ReceiveMessageRequest(queueUrl)
                .withWaitTimeSeconds(2)
                .withMaxNumberOfMessages(1)
        ).messages[0]
    }
}
