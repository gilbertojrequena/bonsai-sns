package io.github.gilbertojrequena.bonsai_sns.server

import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory

public class BonsaiSnsConfig(
    internal var port: Int? = null,
    internal var region: String? = null,
    internal var accountId: String? = null,
    internal var bonsaiSnsEnvironment: BonsaiSnsEnvironment? = null,
    internal var sqsEndpoint: String? = null,
    internal var sqsAccessKey: String? = null,
    internal var sqsSecretKey: String? = null
) {
    companion object {
        private const val DEFAULT_PORT = 7979
        private const val DEFAULT_REGION = "region"
        private const val DEFAULT_ACCOUNT_ID = "123456789"
    }

    init {
        val bonsaiSnsConfig = try {
            ConfigFactory.load().getConfig("bonsai-sns")
        } catch (e: ConfigException.Missing) {
            null
        }
        if (port == null) {
            port = getOrDefault(bonsaiSnsConfig, DEFAULT_PORT) {
                it.getInt("port")
            }
        }
        if (region.isNullOrBlank()) {
            region = getOrDefault(bonsaiSnsConfig, DEFAULT_REGION) {
                it.getString("region")
            }
        }
        if (accountId == null) {
            accountId = getOrDefault(bonsaiSnsConfig, DEFAULT_ACCOUNT_ID) {
                it.getString("accountId")
            }
        }
        if (sqsEndpoint.isNullOrBlank()) {
            sqsEndpoint = getOrDefault(bonsaiSnsConfig, null) {
                it.getString("sqsEndpoint")
            }
        }
        if (sqsAccessKey.isNullOrBlank()) {
            sqsAccessKey = getOrDefault(bonsaiSnsConfig, "accessKey") {
                it.getString("sqsAccessKey")
            }
        }
        if (sqsSecretKey.isNullOrBlank()) {
            sqsSecretKey = getOrDefault(bonsaiSnsConfig, "secretKey") {
                it.getString("sqsSecretKey")
            }
        }
        if (bonsaiSnsEnvironment == null && bonsaiSnsConfig != null) {
            bonsaiSnsEnvironment = bonsaiSnsConfig.toBonsaiSnsEnvironmentDefinition()
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
