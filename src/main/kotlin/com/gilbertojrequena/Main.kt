package com.gilbertojrequena

import com.gilbertojrequena.memsns.server.MemSnsConfig
import com.gilbertojrequena.memsns.server.MemSnsServer

fun main(args: Array<String>) {
    MemSnsServer(configFromArgs(args))
        .start()
}

private fun String.splitToPair(c: Char): Pair<String, String>? = this.indexOf(c).let { index ->
    return when (index) {
        -1 -> null
        else -> Pair(take(index), drop(index + 1))
    }
}

private fun configFromArgs(args: Array<String>): MemSnsConfig {
    val argsMap = args.mapNotNull { it.splitToPair('=') }.toMap()
    return MemSnsConfig(
        port = argsMap["mem-sns.port"]?.toInt(),
        region = argsMap["mem-sns.region"],
        accountId = argsMap["mem-sns.accountId"]?.toLong()
    )
}