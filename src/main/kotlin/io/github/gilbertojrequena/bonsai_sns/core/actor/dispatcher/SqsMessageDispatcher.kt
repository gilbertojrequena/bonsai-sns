package io.github.gilbertojrequena.bonsai_sns.core.actor.dispatcher

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.AmazonSQSException
import com.amazonaws.services.sqs.model.SendMessageRequest
import io.github.gilbertojrequena.bonsai_sns.core.exception.MessageDispatchException
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class SqsMessageDispatcher(private val sqsClient: AmazonSQS?) : MessageDispatcher {
    private val log = KotlinLogging.logger { }
    private val queueUrlByArn: ConcurrentMap<String, String> = ConcurrentHashMap()

    override suspend fun dispatch(endpoint: String, message: String) {
        if (sqsClient == null) {
            log.error { "Trying to send message to SQS but no sqs endpoint is configured, the message will not be delivered" }
            return
        }
        log.debug { "Sending message to SQS, queue: '$endpoint', message: '$message'" }
        try {
            val queueUrl = queueUrlByArn.getOrPut(endpoint) {
                sqsClient.getQueueUrl(getQueueNameFromArn(endpoint)).queueUrl
            }
            sqsClient.sendMessage(SendMessageRequest(queueUrl, message))
            log.debug { "Message sent to SQS successfully, queue: '$endpoint', message: '$message'" }
        } catch (e: AmazonSQSException) {
            log.debug { "Error sending message to SQS, queue: '$endpoint', message: '$message'" }
            throw MessageDispatchException(endpoint, message)
        }
    }

    private fun getQueueNameFromArn(arn: String) = arn.split(":").last()
}