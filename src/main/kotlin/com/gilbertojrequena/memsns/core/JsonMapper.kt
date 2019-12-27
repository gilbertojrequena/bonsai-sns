package com.gilbertojrequena.memsns.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

class JsonMapper private constructor() {
    private val objectMapper = ObjectMapper()

    companion object {
        private val instance = JsonMapper()
        fun instance(): JsonMapper {
            return instance
        }
    }

    fun read(string: String): JsonNode = objectMapper.readTree(string)
}