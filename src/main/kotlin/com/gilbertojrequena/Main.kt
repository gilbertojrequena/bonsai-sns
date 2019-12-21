package com.gilbertojrequena

import com.gilbertojrequena.memsns.server.MemSnsServer

fun main() {
    MemSnsServer.Builder()
        .build()
        .start()
}