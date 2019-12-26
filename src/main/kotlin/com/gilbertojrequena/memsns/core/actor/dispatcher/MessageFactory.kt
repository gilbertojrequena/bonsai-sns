package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.gilbertojrequena.memsns.api.ObjectMapper
import com.gilbertojrequena.memsns.core.SubscriptionWithAttributes
import com.gilbertojrequena.memsns.server.MemSnsConfig
import java.net.URLEncoder

class MessageFactory(private val config: MemSnsConfig) {

    fun create(
        text: String,
        subscriptionWithAttributes: SubscriptionWithAttributes,
        messageId: String
    ): String {
        return if (subscriptionWithAttributes.attributes["RawMessageDelivery"] == "true") {
            text
        } else {
            ObjectMapper.json {
                it.put("Type", "Notification")
                    .put("MessageId", messageId)
                    .put("TopicArn", subscriptionWithAttributes.subscription.topicArn)
                    .put("Message", text)
                    .put("SignatureVersion", "1")
                    .put(
                        "Signature",
                        "IN6nX+TwBMQTIzcQOzQQfLenrCP3vCkOW8owiMtnrUZOigP3faVWMQ6Nsdq1UM5aRTCiWvYUgrBv642k6ryadCLYcc06Issh4QX2JLE0OFgxe51YMLMe27mm2iUv5LTO0uxN3et0vJvwi6bGs1o6Y9R/ypo3RIcN9NOqX0fe+Gp1BFMtyCih/657YgeCMkh+1OJhgX50xLTYQhd9wOK1zNyhOz8C6OEffHIwAfLDTj9Zmh3mzU8L8Ya+hvWayFYSYs+8PfT0JLud5eb3jdAknkN/RFtTYMzaPybS7lif8d697rJBPhlAug5nzoDaF1SxjABCIJcAfGsENV5rvx3MEg=="
                    )
                    .put(
                        "SigningCertURL",
                        encode("https://localhost:${config.port}/SimpleNotificationService-6aad65c2f9911b05cd53efda11f913f9.pem")
                    )
                    .put(
                        "UnsubscribeURL",
                        encode("http://localhost:${config.port}/?Action=Unsubscribe&SubscriptionArn=${subscriptionWithAttributes.subscription.arn}")
                    )
            }
        }
    }

    private fun encode(url: String): String {
        return URLEncoder.encode(
            url,
            "utf-8"
        )
    }
}
