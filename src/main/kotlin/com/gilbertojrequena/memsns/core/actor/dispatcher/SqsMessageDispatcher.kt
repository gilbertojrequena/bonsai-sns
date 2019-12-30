package com.gilbertojrequena.memsns.core.actor.dispatcher

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.QueueDoesNotExistException
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.gilbertojrequena.memsns.server.MemSnsConfig
import mu.KotlinLogging
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class SqsMessageDispatcher(config: MemSnsConfig) : MessageDispatcher {
    private val log = KotlinLogging.logger { }
    private val queueUrlByArn: ConcurrentMap<String, String> = ConcurrentHashMap()
    private var sqsClient: AmazonSQS? = null

    init {
        if (!config.sqsEndpoint.isNullOrBlank()) {
            sqsClient = AmazonSQSClientBuilder.standard()
                .withCredentials(
                    AWSStaticCredentialsProvider(
                        BasicAWSCredentials(config.sqsAccessKey, config.sqsSecretKey)
                    )
                )
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(config.sqsEndpoint, config.region))
                .build()
        }
    }

    override suspend fun dispatch(endpoint: String, message: String) {
        if (sqsClient == null) {
            log.error { "Trying to send message to SQS but no sqs endpoint is configured, the message will not be delivered" }
            return
        }
        log.debug { "Sending message to SQS, queue: '$endpoint', message: '$message'" }
        try {
            val queueUrl = queueUrlByArn.getOrPut(endpoint) {
                sqsClient!!.getQueueUrl(getQueueNameFromArn(endpoint)).queueUrl
            }
            sqsClient!!.sendMessage(SendMessageRequest(queueUrl, message))
            log.debug { "Message sent to SQS successfully, queue: '$endpoint', message: '$message'" }
        } catch (e: QueueDoesNotExistException) {
            log.warn { "Queue with '$endpoint' doesn't exist, message will not be delivered, message: '$message'" }
        } catch (e: Exception) {
            log.error(e) { "Error sending message to SQS, queue: '$endpoint', message: '$message'" }
        }
    }

    private fun getQueueNameFromArn(arn: String) = arn.split(":").last()
}