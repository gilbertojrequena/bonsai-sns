package com.gilbertojrequena.memsns.core.filter_policy

import com.fasterxml.jackson.databind.JsonNode
import com.gilbertojrequena.memsns.core.JsonMapper
import com.gilbertojrequena.memsns.core.exception.InvalidFilterPolicyException

internal class PolicyFilterValidator {
    private val jsonMapper = JsonMapper.instance()

    fun validate(policy: String) {
        if (policy.toByteArray().size > 256000) {
            throw InvalidFilterPolicyException("The maximum allowed size of a policy is 256 KB")
        }
        val policyJson = jsonMapper.read(policy)
        if (policyJson.size() > 5) {
            throw InvalidFilterPolicyException("Filter policy can not have more than 5 key-value pairs")
        }
        if (policyJson.elements().asSequence().map {
                it.size()
            }.reduce { acc, i -> acc * i } > 150) {
            throw InvalidFilterPolicyException("The total combination of values must not exceed 150")
        }
        validateNumbers(policyJson)

        validateNumericValues("", policyJson)
    }

    private fun validateNumericValues(nodeName: String, node: JsonNode) {
        when {
            node.toOp(nodeName) is Numeric -> {
                return generateSequence(0) {
                    it + 2
                }.takeWhile {
                    node[it] != null
                }.forEach {
                    val op = node[it].textValue()
                    if (!setOf("=", ">", ">=", "<", "<=").contains(op)) {
                        throw InvalidFilterPolicyException("Unrecognized numeric range operator: $op")
                    }
                    if (!node[it + 1].isNumber) {
                        throw InvalidFilterPolicyException("Value of $op must be numeric")
                    }
                }
            }
            node.isArray -> {
                node.elements().forEach {
                    validateNumericValues("", it)
                }
            }
            node.isObject -> {
                node.fields().forEach {
                    validateNumericValues(it.key, it.value)
                }
            }
        }
    }

    private fun validateNumbers(node: JsonNode) {
        if (node.isArray) {
            node.elements().forEach {
                validateNumbers(it)
            }
        } else if (node.isObject) {
            node.elements().forEach {
                validateNumbers(it)
            }
        } else if ((node.isNumber || node.isDouble) && (node.doubleValue() < -1.0e9 || node.doubleValue() > 1.0e9)) {
            throw InvalidFilterPolicyException("Value must be between -1.0E9 and 1.0E9, inclusive")
        }
    }
}