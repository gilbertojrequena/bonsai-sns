package com.gilbertojrequena

import com.gilbertojrequena.memsns.api.action.RequestHandler
import com.gilbertojrequena.memsns.api.api
import com.gilbertojrequena.memsns.core.actor.publishActor
import com.gilbertojrequena.memsns.core.actor.snsOpsActor
import com.gilbertojrequena.memsns.core.manager.PublicationManager
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import com.gilbertojrequena.memsns.core.manager.TopicManager
import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

internal fun Application.main() {
    val snsOpsActor = snsOpsActor()
    val snsManager = RequestHandler(
        TopicManager(snsOpsActor),
        SubscriptionManager(snsOpsActor),
        PublicationManager(snsOpsActor, publishActor(snsOpsActor))
    )

    routing {
        api(snsManager)
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, environment = commandLineEnvironment(args)) {

    }.start(wait = true)
}