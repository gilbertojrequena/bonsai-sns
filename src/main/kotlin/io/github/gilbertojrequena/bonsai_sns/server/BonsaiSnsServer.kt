package io.github.gilbertojrequena.bonsai_sns.server

import io.github.gilbertojrequena.bonsai_sns.api.action.RequestHandler
import io.github.gilbertojrequena.bonsai_sns.api.api
import io.github.gilbertojrequena.bonsai_sns.api.exception.InvalidParameterException
import io.github.gilbertojrequena.bonsai_sns.api.exception.MessageAttributeValidationException
import io.github.gilbertojrequena.bonsai_sns.api.invalidParameter
import io.github.gilbertojrequena.bonsai_sns.api.parameterValueInvalid
import io.github.gilbertojrequena.bonsai_sns.core.Subscription
import io.github.gilbertojrequena.bonsai_sns.core.Topic
import io.github.gilbertojrequena.bonsai_sns.core.actor.publishActor
import io.github.gilbertojrequena.bonsai_sns.core.actor.snsOpsActor
import io.github.gilbertojrequena.bonsai_sns.core.manager.PublicationManager
import io.github.gilbertojrequena.bonsai_sns.core.manager.SubscriptionManager
import io.github.gilbertojrequena.bonsai_sns.core.manager.TopicManager
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

class BonsaiSnsServer(private val config: BonsaiSnsConfig) {
    private lateinit var applicationEngine: ApplicationEngine
    private val log = KotlinLogging.logger {}
    private var started = false

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }

    fun start(wait: Boolean = false): BonsaiSnsServer {
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
                port = this@BonsaiSnsServer.config.port!!
            }
        }
        applicationEngine = embeddedServer(Netty, env).start(wait)
        started = true
        return this
    }

    fun start(): BonsaiSnsServer {
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
            config.bonsaiSnsEnvironment.let { envDefinition ->
                log.info { "Initializing environment" }
                envDefinition?.topics?.forEach { topicDefinition ->
                    val topic = topicManager.create(Topic(topicDefinition.name))
                    log.info { "$topic created" }
                    topicDefinition.subscriptions.forEach { subscriptionDefinition ->
                        val subscription = subscriptionManager.create(
                            Subscription(
                                topic.arn, Subscription.Protocol.fromName(subscriptionDefinition.protocol),
                                subscriptionDefinition.endpoint, subscriptionDefinition.owner ?: ""
                            )
                        )
                        log.info { "$subscription added to $topic" }
                        subscriptionDefinition.attributes.forEach {
                            subscriptionManager.setSubscriptionAttribute(subscription.arn, it.key, it.value)
                            log.info { "Attribute '${it.key}'='${it.value}' added to subscription $subscription" }
                        }
                    }
                }
            }
            log.info { "Environment initialization finished" }
        }
    }


    class Builder {
        internal var port: Int? = null
            private set
        internal var accountId: Long? = null
            private set
        internal var region: String? = null
            private set
        internal var bonsaiSnsEnvironment: BonsaiSnsEnvironment? = null
            private set
        internal var sqsEndpoint: String? = null
            private set
        internal var sqsAccessKey: String? = null
            private set
        internal var sqsSecretKey: String? = null
            private set

        fun withPort(port: Int): Builder = apply { this.port = port }
        fun withAccountId(accountId: Long): Builder = apply { this.accountId = accountId }
        fun withRegion(region: String): Builder = apply { this.region = region }
        fun withBonsaiSnsEnvironmentDefinition(bonsaiSnsEnvironment: BonsaiSnsEnvironment): Builder =
            apply { this.bonsaiSnsEnvironment = bonsaiSnsEnvironment }

        fun withSqsEndpoint(sqsEndpoint: String): Builder = apply { this.sqsEndpoint = sqsEndpoint }
        fun withSqsAccessKey(sqsAccessKey: String): Builder = apply { this.sqsAccessKey = sqsAccessKey }
        fun withSqsSecretKey(sqsSecretKey: String): Builder = apply { this.sqsSecretKey = sqsSecretKey }

        fun start(): BonsaiSnsServer {
            return BonsaiSnsServer(
                BonsaiSnsConfig(port, region, accountId, bonsaiSnsEnvironment, sqsEndpoint, sqsAccessKey, sqsSecretKey)
            ).start()
        }
    }
}