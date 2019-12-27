package com.gilbertojrequena.memsns.server

import com.gilbertojrequena.memsns.api.action.RequestHandler
import com.gilbertojrequena.memsns.api.api
import com.gilbertojrequena.memsns.api.exception.InvalidParameterException
import com.gilbertojrequena.memsns.api.exception.MessageAttributeValidationException
import com.gilbertojrequena.memsns.api.invalidParameter
import com.gilbertojrequena.memsns.api.parameterValueInvalid
import com.gilbertojrequena.memsns.core.Subscription
import com.gilbertojrequena.memsns.core.Topic
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.util.concurrent.TimeUnit

class MemSnsServer(private val config: MemSnsConfig) {
    private lateinit var applicationEngine: ApplicationEngine
    private val log = KotlinLogging.logger {}
    private var started = false

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    fun start(wait: Boolean = false): MemSnsServer {
        if (started) {
            return this
        }
        val snsOpsActor = snsOpsActor()
        val subscriptionManager = SubscriptionManager(snsOpsActor, config)
        val topicManager = TopicManager(snsOpsActor, config)
        val requestHandler = RequestHandler(
            topicManager,
            subscriptionManager,
            PublicationManager(publishActor(subscriptionManager, config)),
            config
        )
        initializeEnvironment(topicManager, subscriptionManager)

        val env = applicationEngineEnvironment {
            module {
                install(StatusPages) {
                    invalidParameter<InvalidParameterException>()
                    parameterValueInvalid<MessageAttributeValidationException>()
                }
                routing {
                    api(requestHandler)
                }
            }
            connector {
                host = "0.0.0.0"
                port = this@MemSnsServer.config.port!!
            }
        }
        applicationEngine = embeddedServer(Netty, env).start(wait)
        started = true
        return this
    }

    fun start(): MemSnsServer {
        return start(false)
    }

    fun stop() {
        applicationEngine.stop(1, 5, TimeUnit.SECONDS)
    }

    private fun initializeEnvironment(
        topicManager: TopicManager,
        subscriptionManager: SubscriptionManager
    ) {
        GlobalScope.launch {
            config.memSnsEnvironment.let { envDefinition ->
                log.debug { "Initializing environment" }
                envDefinition?.topics?.forEach { topicDefinition ->
                    val topic = topicManager.create(Topic(topicDefinition.name))
                    log.debug { "$topic created" }
                    topicDefinition.subscriptions.forEach { subscriptionDefinition ->
                        val subscription = subscriptionManager.create(
                            Subscription(
                                topic.arn, Subscription.Protocol.fromName(subscriptionDefinition.protocol),
                                subscriptionDefinition.endpoint, subscriptionDefinition.owner ?: ""
                            )
                        )
                        log.debug { "$subscription added to $topic" }
                        subscriptionDefinition.attributes.forEach {
                            subscriptionManager.setSubscriptionAttribute(subscription.arn, it.key, it.value)
                            log.debug { "Attribute '${it.key}'='${it.value}' added to subscription $subscription" }
                        }
                    }
                }
            }
            log.debug { "Environment initialization finished" }
        }
    }


    class Builder {
        internal var port: Int? = null
            private set
        internal var accountId: Long? = null
            private set
        internal var region: String? = null
            private set
        internal var memSnsEnvironment: MemSnsEnvironment? = null
            private set

        fun withPort(port: Int): Builder = apply { this.port = port }
        fun withAccountId(accountId: Long): Builder = apply { this.accountId = accountId }
        fun withRegion(region: String): Builder = apply { this.region = region }
        fun withMemSnsEnvironmentDefinition(memSnsEnvironment: MemSnsEnvironment): Builder =
            apply { this.memSnsEnvironment = memSnsEnvironment }

        fun start(): MemSnsServer {
            return MemSnsServer(MemSnsConfig(port, region, accountId, memSnsEnvironment)).start()
        }
    }
}