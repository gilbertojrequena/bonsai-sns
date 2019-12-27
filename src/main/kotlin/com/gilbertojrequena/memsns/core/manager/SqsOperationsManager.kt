package com.gilbertojrequena.memsns.core.manager

import com.gilbertojrequena.memsns.core.actor.message.SnsOpsMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

internal abstract class SqsOperationsManager(private val snsOpActor: SendChannel<SnsOpsMessage>) {

    suspend fun <T> sendToActorAndReceive(buildMessage: (Channel<T>) -> SnsOpsMessage): T {
        val responseChannel = Channel<T>()
        snsOpActor.send(buildMessage(responseChannel))
        return responseChannel.receive()
    }
}