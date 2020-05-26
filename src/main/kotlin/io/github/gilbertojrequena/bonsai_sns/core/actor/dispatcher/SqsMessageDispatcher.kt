package io.github.gilbertojrequena.bonsai_sns.core.actor.dispatcher

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.AmazonSQSException
import com.amazonaws.services.sqs.model.MessageAttributeValue
import com.amazonaws.services.sqs.model.SendMessageRequest
import io.github.gilbertojrequena.bonsai_sns.core.exception.MessageDispatchException
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class SqsMessageDispatcher(
    private val sqsClient: AmazonSQS?,
    private val messageBodyFactory: MessageBodyFactory
) : MessageDispatcher {
    private val log = KotlinLogging.logger { }
    private val queueUrlByArn: ConcurrentMap<String, String> = ConcurrentHashMap()

    override suspend fun dispatch(dispatchMessageRequest: DispatchMessageRequest) {
        val endpoint = dispatchMessageRequest.endpoint
        if (sqsClient == null) {
            log.error { "Trying to send message to SQS but no sqs endpoint is configured, the message will not be delivered" }
            return
        }
        val messageBody = messageBodyFactory.create(dispatchMessageRequest)
        log.debug { "Sending message to SQS, queue: '$endpoint', message: '$messageBody'" }
        try {
            val queueUrl = queueUrlByArn.getOrPut(endpoint) {
                sqsClient.getQueueUrl(getQueueNameFromArn(endpoint)).queueUrl
            }
            val sendMessageRequest = SendMessageRequest(queueUrl, messageBody)
            if (dispatchMessageRequest.isRaw) {
                sendMessageRequest.withMessageAttributes(dispatchMessageRequest.message.attributes.mapValues {
                    MessageAttributeValue().withDataType(it.value.type).withStringValue(it.value.value)
                })
            }
            sqsClient.sendMessage(sendMessageRequest)
            log.debug { "Message sent to SQS successfully, queue: '$endpoint', message: '$messageBody'" }
        } catch (e: AmazonSQSException) {
            log.debug { "Error sending message to SQS, queue: '$endpoint', message: '$messageBody'" }
            throw MessageDispatchException(endpoint, messageBody)
        }
    }

    private fun getQueueNameFromArn(arn: String) = arn.split(":").last()
}
