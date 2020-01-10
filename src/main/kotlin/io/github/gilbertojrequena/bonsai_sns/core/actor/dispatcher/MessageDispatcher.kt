package io.github.gilbertojrequena.bonsai_sns.core.actor.dispatcher

internal interface MessageDispatcher {
    suspend fun dispatch(endpoint: String, message: String)
}