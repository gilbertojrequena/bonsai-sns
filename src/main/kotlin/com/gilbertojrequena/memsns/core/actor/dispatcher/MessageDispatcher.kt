package com.gilbertojrequena.memsns.core.actor.dispatcher

interface MessageDispatcher {
    suspend fun dispatch(endpoint: String, message: String)
}