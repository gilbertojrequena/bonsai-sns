package com.gilbertojrequena.memsns.core.actor.dispatcher

internal interface MessageDispatcher {
    suspend fun dispatch(endpoint: String, message: String)
}