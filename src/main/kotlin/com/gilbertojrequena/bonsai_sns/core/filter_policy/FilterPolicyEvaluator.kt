package com.gilbertojrequena.bonsai_sns.core.filter_policy

import com.gilbertojrequena.bonsai_sns.core.JsonMapper
import com.gilbertojrequena.bonsai_sns.core.MessageAttribute
import mu.KotlinLogging

internal class FilterPolicyEvaluator {

    private val log = KotlinLogging.logger {}
    private val jsonMapper = JsonMapper.instance()

    fun eval(attributes: Map<String, MessageAttribute>, policy: String): Boolean {
        log.debug { "Evaluating $policy with $attributes" }
        val policyJson = jsonMapper.read(policy)
        policyJson.fields().forEach {
            if (!it.value.toOp(it.key).apply(attributes[it.key])) {
                log.debug { "Evaluation failed for condition ${it.value} with $attributes" }
                return false
            }
        }
        log.debug { "Evaluation for $policy with $attributes succeeded" }
        return true
    }
}