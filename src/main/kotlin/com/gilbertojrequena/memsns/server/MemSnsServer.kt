package com.gilbertojrequena.memsns.server

import com.gilbertojrequena.memsns.api.action.RequestHandler
import com.gilbertojrequena.memsns.api.api
import com.gilbertojrequena.memsns.api.exception.InvalidParameterException
import com.gilbertojrequena.memsns.api.invalidParameter
import com.gilbertojrequena.memsns.core.Config
import com.gilbertojrequena.memsns.core.actor.publishActor
import com.gilbertojrequena.memsns.core.actor.snsOpsActor
import com.gilbertojrequena.memsns.core.manager.PublicationManager
import com.gilbertojrequena.memsns.core.manager.SubscriptionManager
import com.gilbertojrequena.memsns.core.manager.TopicManager
import io.ktor.application.install
import io.ktor.features.StatusPages
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.util.concurrent.TimeUnit

class MemSnsServer private constructor(builder: Builder) {
    private val config: Config
    private lateinit var applicationEngine: ApplicationEngine

    init {
        this.config = Config(
            builder.port ?: 7979,
            builder.region ?: "memsns-region",
            builder.accountId ?: 123456789
        )
    }

    fun start(wait: Boolean = false): MemSnsServer {
        val snsOpsActor = snsOpsActor()
        val subscriptionManager = SubscriptionManager(snsOpsActor, config)
        val snsManager = RequestHandler(
            TopicManager(snsOpsActor, config),
            subscriptionManager,
            PublicationManager(publishActor(subscriptionManager, config)),
            config
        )
        val env = applicationEngineEnvironment {
            module {
                install(StatusPages) {
                    invalidParameter<InvalidParameterException>()
                }
                routing {
                    api(snsManager)
                }
            }
            connector {
                host = "0.0.0.0"
                port = this@MemSnsServer.config.port
            }
        }
        applicationEngine = embeddedServer(Netty, env).start(wait)
        return this
    }

    fun stop() {
        applicationEngine.stop(1, 5, TimeUnit.SECONDS)
    }

    class Builder {
        internal var port: Int? = null
            private set
        internal var accountId: Long? = null
            private set
        internal var region: String? = null
            private set

        fun port(port: Int): Builder = apply { this.port = port }
        fun accountId(accountId: Long): Builder = apply { this.accountId = accountId }
        fun region(region: String): Builder = apply { this.region = region }
        fun build(): MemSnsServer = MemSnsServer(this)
    }
}