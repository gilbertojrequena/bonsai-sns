package com.gilbertojrequena.memsns.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.jdom2.Element
import org.jdom2.output.XMLOutputter
import org.jonnyzzz.kotlin.xml.dsl.XWriter


object ObjectMapper {
    private val xmlMapper = XMLOutputter()
    private val jsonMapper = ObjectMapper()
    fun writeXmlElement(element: Element): String = xmlMapper.outputString(element)

    fun jsonObjectBuilder(): ObjectNode = jsonMapper.createObjectNode()

    fun json(block: (builder: ObjectNode) -> ObjectNode): String {
        return jsonMapper.writeValueAsString(block(jsonMapper.createObjectNode()))
    }

    fun jsonArrayBuilder(): ArrayNode = jsonMapper.createArrayNode()
}

fun XWriter.awsMetadata(requestId: String = "00000000-0000-0000-0000-000000000000") {
    element("ResponseMetadata") {
        element("RequestId") {
            text(requestId)
        }
    }
}