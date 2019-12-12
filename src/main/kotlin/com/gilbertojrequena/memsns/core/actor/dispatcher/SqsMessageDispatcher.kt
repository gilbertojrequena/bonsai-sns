package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.core.SnsHttpClient
import com.gilbertojrequena.memsns.core.Subscription
import mu.KotlinLogging

class SqsMessageDispatcher(private val httpClient: SnsHttpClient) : MessageDispatcher {

    private val log = KotlinLogging.logger { }

    override suspend fun publish(subscription: Subscription, message: String, messageId: String) {
        log.debug { "Publishing: <$message> to subscription: $subscription" }
        val messageBody = ObjectMapper.json {
            it.put("Type", "Notification")
                .put("MessageId", messageId)
                .put("TopicArn", subscription.topicArn)
                .put("Message", message)
                .put("SignatureVersion", "1")
                .put(
                    "Signature",
                    "IN6nX+TwBMQTIzcQOzQQfLenrCP3vCkOW8owiMtnrUZOigP3faVWMQ6Nsdq1UM5aRTCiWvYUgrBv642k6ryadCLYcc06Issh4QX2JLE0OFgxe51YMLMe27mm2iUv5LTO0uxN3et0vJvwi6bGs1o6Y9R/ypo3RIcN9NOqX0fe+Gp1BFMtyCih/657YgeCMkh+1OJhgX50xLTYQhd9wOK1zNyhOz8C6OEffHIwAfLDTj9Zmh3mzU8L8Ya+hvWayFYSYs+8PfT0JLud5eb3jdAknkN/RFtTYMzaPybS7lif8d697rJBPhlAug5nzoDaF1SxjABCIJcAfGsENV5rvx3MEg=="
                )
                .put(
                    "SigningCertURL",
                    "https://localhost:8080/SimpleNotificationService-6aad65c2f9911b05cd53efda11f913f9.pem" //TODO fix port
                )
                .put(
                    "UnsubscribeURL",
                    "http://localhost:8080/?Action=Unsubscribe&SubscriptionArn=${subscription.arn}" //TODO fix port
                )
        }
        httpClient.post(
            "${subscription.endpoint}?Action=SendMessage&MessageBody=${messageBody}&Version=2012-11-05",
            ""
        )
        log.debug { "Finished publishing: <$message> to subscription: $subscription" }
    }
}