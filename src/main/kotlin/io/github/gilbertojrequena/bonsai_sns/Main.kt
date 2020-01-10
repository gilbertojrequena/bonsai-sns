package io.github.gilbertojrequena.bonsai_sns

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
        port = argsMap["port"]?.toInt(),
        region = argsMap["region"],
        accountId = argsMap["accountId"]?.toLong(),
        sqsEndpoint = argsMap["sqsEndpoint"],
        sqsAccessKey = argsMap["sqsAccessKey"],
        sqsSecretKey = argsMap["sqsSecretKey"]
    )
}