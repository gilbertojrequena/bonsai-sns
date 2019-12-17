package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.SubscriptionWithAttributes
import io.ktor.config.ApplicationConfig
import io.ktor.config.ApplicationConfigValue
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class MessageFactoryTest {

    private val portConf = object : ApplicationConfigValue {
        override fun getString(): String = "8080"
        override fun getList(): List<String> = listOf()
    }
    private val config = mockk<ApplicationConfig>()
    private val subscription = mockk<Subscription>()
    private lateinit var messageFactory: MessageFactory

    @BeforeEach
    fun setUp() {
        every {
            config.property("ktor.deployment.port")
        } returns portConf
        every {
            subscription.topicArn
        } returns "topic-arn"
        every {
            subscription.arn
        } returns "subscription-arn"
        messageFactory = MessageFactory(config)
    }

    @Test
    fun `should create raw message`() {
        val message = messageFactory.create(
            "message",
            SubscriptionWithAttributes(subscription, mapOf("RawMessageDelivery" to "true")),
            "id"
        )
        assertEquals("message", message)
    }

    @Test
    fun `should create message with attributes`() {
        val message = messageFactory.create(
            "message",
            SubscriptionWithAttributes(subscription, mapOf()),
            "id"
        )
        assertEquals(
            """{"Type":"Notification","MessageId":"id","TopicArn":"topic-arn","Message":"message","SignatureVersion":"1","Signature":"IN6nX+TwBMQTIzcQOzQQfLenrCP3vCkOW8owiMtnrUZOigP3faVWMQ6Nsdq1UM5aRTCiWvYUgrBv642k6ryadCLYcc06Issh4QX2JLE0OFgxe51YMLMe27mm2iUv5LTO0uxN3et0vJvwi6bGs1o6Y9R/ypo3RIcN9NOqX0fe+Gp1BFMtyCih/657YgeCMkh+1OJhgX50xLTYQhd9wOK1zNyhOz8C6OEffHIwAfLDTj9Zmh3mzU8L8Ya+hvWayFYSYs+8PfT0JLud5eb3jdAknkN/RFtTYMzaPybS7lif8d697rJBPhlAug5nzoDaF1SxjABCIJcAfGsENV5rvx3MEg==","SigningCertURL":"https://localhost:8080/SimpleNotificationService-6aad65c2f9911b05cd53efda11f913f9.pem","UnsubscribeURL":"http://localhost:8080/?Action=Unsubscribe&SubscriptionArn=subscription-arn"}""",
            message
        )
    }
}