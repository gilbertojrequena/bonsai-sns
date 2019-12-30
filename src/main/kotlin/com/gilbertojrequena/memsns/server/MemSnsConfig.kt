package com.gilbertojrequena.memsns.server

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory

public class MemSnsConfig(
    internal var port: Int? = null,
    internal var region: String? = null,
    internal var accountId: Long? = null,
    internal var memSnsEnvironment: MemSnsEnvironment? = null,
    internal var sqsEndpoint: String? = null,
    internal var sqsAccessKey: String? = null,
    internal var sqsSecretKey: String? = null
) {
    companion object {
        private const val DEFAULT_PORT = 7979
        private const val DEFAULT_REGION = "region"
        private const val DEFAULT_ACCOUNT_ID = 123456789L
    }

    init {
        val memSnsConfig = try {
            ConfigFactory.load().getConfig("mem-sns")
        } catch (e: ConfigException.Missing) {
            null
        }
        if (port == null) {
            port = getOrDefault(memSnsConfig, DEFAULT_PORT) {
                it.getInt("port")
            }
        }
        if (region.isNullOrBlank()) {
            region = getOrDefault(memSnsConfig, DEFAULT_REGION) {
                it.getString("region")
            }
        }
        if (accountId == null) {
            accountId = getOrDefault(memSnsConfig, DEFAULT_ACCOUNT_ID) {
                it.getLong("accountId")
            }
        }
        if (sqsEndpoint.isNullOrBlank()) {
            sqsEndpoint = getOrDefault(memSnsConfig, null) {
                it.getString("sqsEndpoint")
            }
        }
        if (sqsAccessKey.isNullOrBlank()) {
            sqsAccessKey = getOrDefault(memSnsConfig, "accessKey") {
                it.getString("sqsAccessKey")
            }
        }
        if (sqsSecretKey.isNullOrBlank()) {
            sqsSecretKey = getOrDefault(memSnsConfig, "secretKey") {
                it.getString("sqsSecretKey")
            }
        }
        if (memSnsEnvironment == null && memSnsConfig != null) {
            memSnsEnvironment = memSnsConfig.toMemSnsEnvironmentDefinition()
        }
    }

    private fun <T> getOrDefault(config: Config?, default: T, block: (config: Config) -> T): T {
        return if (config != null) try {
            block(config)
        } catch (e: ConfigException.Missing) {
            default
        } else default
    }
}
