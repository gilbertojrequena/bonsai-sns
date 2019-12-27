package com.gilbertojrequena.memsns.core.filter_policy

import com.fasterxml.jackson.databind.JsonNode
import com.gilbertojrequena.memsns.core.MessageAttribute
import com.gilbertojrequena.memsns.core.exception.InvalidAttributeTypeException
import com.gilbertojrequena.memsns.core.exception.InvalidPolicyNumericOperationException
import java.math.BigDecimal

internal val operationFactory = OperationFactory()

internal fun JsonNode.toOp(nodeName: String? = null): Operation {
    return operationFactory.create(nodeName, this)
}

internal interface Operation {
    fun apply(attribute: MessageAttribute?): Boolean
}

internal class AnythingBut(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        if (attribute == null) {
            return false
        }
        return when (attribute.type) {
            "String.Array" -> {
                return checkBlackListed(toStringList(attribute.value)) {
                    if (it.isBoolean) {
                        it.booleanValue()
                    }
                    it.textValue()
                }
            }
            "Number.Array" -> {
                return checkBlackListed(toBigDecimalList(attribute.value)) {
                    it.decimalValue()
                }
            }
            else -> !node.toOp().apply(attribute)
        }
    }

    private fun <T> checkBlackListed(values: List<T>, blackListElementMapper: (JsonNode) -> T?): Boolean {
        val blackList = node.elements().asSequence().toList().map(blackListElementMapper)
        return values.map { !blackList.contains(it) }.reduce { acc, b -> acc.or(b) }
    }

    private fun toStringList(value: String): List<String?> {
        return value.trim().removePrefix("[").removeSuffix("]").split(",").map {
            it.trim().removeSurrounding("\"")
        }.map { if (it == "null") null else it }
    }

    private fun toBigDecimalList(value: String): List<BigDecimal?> {
        return toStringList(value).map { it?.toBigDecimal() }
    }
}

internal class Numeric(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        if (attribute == null) {
            return false
        }
        return generateSequence(0) {
            it + 2
        }.takeWhile {
            node[it] != null
        }.map {
            when (node[it].textValue()) {
                "=" -> attribute.value.toBigDecimal() == node[it + 1].decimalValue()
                ">" -> attribute.value.toBigDecimal() > node[it + 1].decimalValue()
                ">=" -> attribute.value.toBigDecimal() >= node[it + 1].decimalValue()
                "<" -> attribute.value.toBigDecimal() < node[it + 1].decimalValue()
                "<=" -> attribute.value.toBigDecimal() <= node[it + 1].decimalValue()
                else -> throw InvalidPolicyNumericOperationException(node[it].textValue())
            }
        }.reduce { acc, b -> acc.and(b) }
    }
}

internal class Prefix(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        if (attribute == null) {
            return false
        }
        return attribute.value.startsWith(node.textValue())
    }
}

internal class Exist(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean =
        node.booleanValue() && attribute != null || !node.booleanValue() && attribute == null
}

internal class Equal(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        if (attribute == null) {
            return false
        }
        return when (attribute.type) {
            "String" -> attribute.value == node.textValue()
            "Number" -> attribute.value.toBigDecimal() == node.decimalValue()
            else -> throw InvalidAttributeTypeException(attribute.type)
        }
    }
}

internal class Or(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        return node.elements().asSequence().map {
            it.toOp().apply(attribute)
        }.reduce { acc, b -> acc.or(b) }
    }
}

internal class And(private val node: JsonNode) : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        return node.fields().asSequence().map {
            it.value.toOp(it.key).apply(attribute)
        }.reduce { acc, b -> acc.and(b) }
    }
}

internal class NotNullAttribute : Operation {
    override fun apply(attribute: MessageAttribute?): Boolean {
        return attribute != null
    }
}

internal class OperationFactory {
    fun create(nodeName: String?, node: JsonNode): Operation {
        return when (nodeName) {
            "anything-but" -> AnythingBut(node)
            "numeric" -> Numeric(node)
            "prefix" -> Prefix(node)
            "exists" -> Exist(node)
            else -> {
                return if (node.isTextual || node.isNumber) {
                    Equal(node)
                } else if (node.isArray) {
                    Or(node)
                } else if (node.isObject) {
                    And(node)
                } else {
                    NotNullAttribute()
                }
            }
        }
    }
}