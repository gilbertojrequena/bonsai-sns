package io.github.gilbertojrequena.bonsai_sns.api.action

import com.gilbertojrequena.bonsai_sns.api.action
import com.gilbertojrequena.bonsai_sns.api.exception.ActionNotFoundException
import com.gilbertojrequena.bonsai_sns.core.manager.PublicationManager
import com.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import com.gilbertojrequena.bonsai_sns.core.manager.TopicManager
import com.gilbertojrequena.bonsai_sns.server.BonsaiSnsConfig
import io.ktor.application.ApplicationCall
import io.ktor.http.Parameters
import io.ktor.request.receive
import mu.KotlinLogging

internal class RequestHandler(
    topicManager: TopicManager,
    subscriptionManager: SubscriptionManager,
    publicationManager: PublicationManager,
    config: BonsaiSnsConfig
) {
    private val log = KotlinLogging.logger {}
    private val actions: Map<String, Action> = mapOf(
        "AddPermission" to AddPermission(),
        "CheckIfPhoneNumberIsOptedOut" to CheckIfPhoneNumberIsOptedOut(),
        "ConfirmSubscription" to ConfirmSubscription(config),
        "CreatePlatformApplication" to CreatePlatformApplication(config),
        "CreatePlatformEndpoint" to CreatePlatformEndpoint(config),
        "CreateTopic" to CreateTopic(topicManager),
        "DeleteEndpoint" to DeleteEndpoint(),
        "DeletePlatformApplication" to DeletePlatformApplication(),
        "DeleteTopic" to DeleteTopic(topicManager),
        "GetEndpointAttributes" to GetEndpointAttributes(),
        "GetPlatformApplicationAttributes" to GetPlatformApplicationAttributes(),
        "GetSMSAttributes" to GetSMSAttributes(),
        "GetSubscriptionAttributes" to GetSubscriptionAttributes(subscriptionManager),
        "GetTopicAttributes" to GetTopicAttributes(),
        "ListEndpointsByPlatformApplication" to ListEndpointsByPlatformApplication(),
        "ListPhoneNumbersOptedOut" to ListPhoneNumbersOptedOut(),
        "ListPlatformApplications" to ListPlatformApplications(),
        "ListSubscriptions" to ListSubscriptions(
            subscriptionManager
        ),
        "ListSubscriptionsByTopic" to ListSubscriptionsByTopic(
            subscriptionManager
        ),
        "ListTagsForResource" to ListTagsForResource(),
        "ListTopics" to ListTopics(topicManager),
        "OptInPhoneNumber" to OptInPhoneNumber(),
        "Publish" to Publish(publicationManager),
        "RemovePermission" to RemovePermission(),
        "SetEndpointAttributes" to SetEndpointAttributes(),
        "SetPlatformApplicationAttributes" to SetPlatformApplicationAttributes(),
        "SetSMSAttributes" to SetSMSAttributes(),
        "SetSubscriptionAttributes" to SetSubscriptionAttributes(subscriptionManager),
        "SetTopicAttributes" to SetTopicAttributes(),
        "Subscribe" to Subscribe(subscriptionManager),
        "TagResource" to TagResource(),
        "Unsubscribe" to Unsubscribe(subscriptionManager),
        "UntagResource" to UntagResource()
    )

    suspend fun processRequest(call: ApplicationCall) {
        val params = call.receive<Parameters>()
        val action = actions[params.action()] ?: throw ActionNotFoundException(params.action())
        log.debug { "Executing action: ${action.javaClass.simpleName} with params: $params" }
        action.execute(call, params)
    }
}