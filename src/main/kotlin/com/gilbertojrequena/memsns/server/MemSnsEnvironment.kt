package com.gilbertojrequena.memsns.server

import com.gilbertojrequena.memsns.core.Attributes
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigObject

internal fun Config.toMemSnsEnvironmentDefinition(): MemSnsEnvironment? {
    return this.getObjectOrNull("topics")
        ?.toTopicsDefinition()?.let {
            MemSnsEnvironment.definition()
                .withTopics(it)
        }
}

internal fun ConfigObject.toTopicsDefinition(): List<Topic> {
    return this.keys
        .map {
            Topic.definition()
                .withName(it)
                .withSubscriptions(
                    this.toConfig()
                        .getObjectList("$it.subscriptions")
                        .map(ConfigObject::toConfig)
                        .map(Config::toSubscriptionDefinition)
                )
        }
}

internal fun Config.toSubscriptionDefinition(): Subscription {
    val attributes = this.getObjectOrNull("attributes")?.unwrapped()
    val subscriptionDefinition = Subscription.definition()
        .withEndpoint(this.getString("endpoint"))
        .withProtocol(this.getString("protocol"))
        .withOwner(this.getString("owner"))
    attributes?.let { subscriptionDefinition.withAttributes(it as Map<String, String>) }
    return subscriptionDefinition
}

internal fun Config.getObjectOrNull(key: String): ConfigObject? {
    return try {
        this.getObject(key)
    } catch (e: ConfigException.Missing) {
        null
    }
}

class MemSnsEnvironment private constructor() {
    companion object {
        fun definition(): MemSnsEnvironment {
            return MemSnsEnvironment()
        }
    }

    internal var topics: MutableList<Topic> = mutableListOf()
        private set

    fun withTopic(topic: Topic): MemSnsEnvironment =
        apply { topics.add(topic) }

    fun withTopics(topics: List<Topic>): MemSnsEnvironment =
        apply { this.topics = topics.toMutableList() }


}

class Topic private constructor() {
    companion object {
        fun definition(): Topic {
            return Topic()
        }
    }

    internal lateinit var name: String
        private set
    internal var subscriptions: MutableList<Subscription> = mutableListOf()

    fun withName(name: String): Topic = apply { this.name = name }

    fun withSubscription(subscription: Subscription): Topic =
        apply { subscriptions.add(subscription) }

    fun withSubscriptions(subscriptions: List<Subscription>): Topic =
        apply { this.subscriptions = subscriptions.toMutableList() }
}

class Subscription private constructor() {

    companion object {
        fun definition(): Subscription {
            return Subscription()
        }
    }

    internal lateinit var endpoint: String
        private set
    internal lateinit var protocol: String
        private set
    internal var owner: String? = null
        private set
    internal var attributes: Attributes = mutableMapOf()

    fun withEndpoint(endpoint: String): Subscription = apply { this.endpoint = endpoint }

    fun withProtocol(protocol: String): Subscription = apply { this.protocol = protocol }

    fun withOwner(owner: String?): Subscription = apply { this.owner = owner }

    fun withAttributes(attributes: Attributes): Subscription = apply { this.attributes = attributes }

    fun withAttribute(key: String, value: String): Subscription =
        apply { this.attributes += Pair(key, value) }
}