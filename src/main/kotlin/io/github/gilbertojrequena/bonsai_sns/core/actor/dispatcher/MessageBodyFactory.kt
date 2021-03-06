package io.github.gilbertojrequena.bonsai_sns.core.actor.dispatcher

import io.github.gilbertojrequena.bonsai_sns.api.ObjectMapper
import io.github.gilbertojrequena.bonsai_sns.server.BonsaiSnsConfig
import java.net.URLEncoder

internal class MessageBodyFactory(private val config: BonsaiSnsConfig) {

    fun create(dispatchMessageRequest: DispatchMessageRequest): String {
        return if (dispatchMessageRequest.isRaw) {
            dispatchMessageRequest.message.body
        } else {
            ObjectMapper.json { jsonObject ->
                val attributes = jsonObject.put("Type", "Notification")
                    .put("MessageId", dispatchMessageRequest.messageId)
                    .put("TopicArn", dispatchMessageRequest.topicArn)
                    .put("Message", dispatchMessageRequest.message.body)
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
                        encode("http://localhost:${config.port}/?Action=Unsubscribe&SubscriptionArn=${dispatchMessageRequest.subscriptionArn}")
                    )
                    .putObject("MessageAttributes")

                for (attribute in dispatchMessageRequest.message.attributes) {
                    attributes.putObject(attribute.key)
                        .put("Type", attribute.value.type)
                        .put("Value", attribute.value.value)
                }
                jsonObject
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
