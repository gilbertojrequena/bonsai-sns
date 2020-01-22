package io.github.gilbertojrequena.bonsai_sns.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode


internal object ObjectMapper {
    private val jsonMapper = ObjectMapper()

    fun json(block: (builder: ObjectNode) -> ObjectNode): String {
        return jsonMapper.writeValueAsString(block(jsonMapper.createObjectNode()))
    }
}