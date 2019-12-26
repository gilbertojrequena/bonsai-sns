package com.gilbertojrequena.memsns.server

import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory

class MemSnsConfig(
    internal var port: Int? = null,
    internal var region: String? = null,
    internal var accountId: Long? = null,
    internal var memSnsEnvironment: MemSnsEnvironment? = null
) {
    companion object {
        private const val DEFAULT_PORT = 7979
        private const val DEFAULT_REGION = "region"
        private const val DEFAULT_ACCOUNT_ID = 123456789L
    }

    init {
        val rootConfig = ConfigFactory.load()
        val hasConfig = rootConfig.hasPath("mem-sns")
        if (port == null) {
            port = if (hasConfig) try {
                rootConfig.getConfig("mem-sns").getInt("port")
            } catch (e: ConfigException.Missing) {
                DEFAULT_PORT
            } else DEFAULT_PORT
        }
        if (region == null) {
            region = if (hasConfig) try {
                rootConfig.getConfig("mem-sns").getString("region")
            } catch (e: ConfigException.Missing) {
                DEFAULT_REGION
            } else DEFAULT_REGION
        }
        if (accountId == null) {
            accountId = if (hasConfig) try {
                rootConfig.getConfig("mem-sns").getLong("accountId")
            } catch (e: ConfigException.Missing) {
                DEFAULT_ACCOUNT_ID
            } else DEFAULT_ACCOUNT_ID
        }
        if (memSnsEnvironment == null && hasConfig) {
            memSnsEnvironment = rootConfig.getConfig("mem-sns").toMemSnsEnvironmentDefinition()
        }
    }
}
