package com.gilbertojrequena.bonsai_sns

import com.gilbertojrequena.bonsai_sns.server.BonsaiSnsConfig
import com.gilbertojrequena.bonsai_sns.server.BonsaiSnsServer

fun main(args: Array<String>) {
    BonsaiSnsServer(configFromArgs(args))
        .start()
}

private fun String.splitToPair(c: Char): Pair<String, String>? = this.indexOf(c).let { index ->
    return when (index) {
        -1 -> null
        else -> Pair(take(index), drop(index + 1))
    }
}

private fun configFromArgs(args: Array<String>): BonsaiSnsConfig {
    val argsMap = args.mapNotNull { it.splitToPair('=') }.toMap()
    return BonsaiSnsConfig(
        port = argsMap["bonsai-sns.port"]?.toInt(),
        region = argsMap["bonsai-sns.region"],
        accountId = argsMap["bonsai-sns.accountId"]?.toLong(),
        sqsEndpoint = argsMap["bonsai-sns.sqsEndpoint"],
        sqsAccessKey = argsMap["bonsai-sns.sqsAccessKey"],
        sqsSecretKey = argsMap["bonsai-sns.sqsSecretKey"]
    )
}