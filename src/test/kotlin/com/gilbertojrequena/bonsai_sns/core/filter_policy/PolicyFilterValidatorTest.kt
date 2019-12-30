package com.gilbertojrequena.bonsai_sns.core.filter_policy

import com.gilbertojrequena.bonsai_sns.core.exception.InvalidFilterPolicyException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PolicyFilterValidatorTest {

    private val validator = PolicyFilterValidator()

    @Test
    fun `should accept up to 5 attributes`() {
        validator.validate(
            """{
                       "key_a": ["value_one", "value_two", "value_three"],
                       "key_b": ["value_one"],
                       "key_c": ["value_one", "value_two"],
                       "key_d": ["value_one", "value_two"]
                    }"""
        )
    }

    @Test
    fun `should throw exception when more than 5 attributes are provided`() {
        assertThrows<InvalidFilterPolicyException> {
            validator.validate(
                """{
                       "key_a": ["value_one", "value_two", "value_three"],
                       "key_b": ["value_one"],
                       "key_c": ["value_one", "value_two"],
                       "key_d": ["value_one", "value_two"],
                       "key_e": ["value_one", "value_two"],
                       "key_f": ["value_one", "value_two"]
                    }"""
            )
        }
    }

    @Test
    fun `should throw exception when more than 150 value combinations are provided`() {
        assertThrows<InvalidFilterPolicyException> {
            validator.validate(
                """{
                       "key_a": ["value_one", "value_two", "value_three", "a", "a", "a", "a", "a", "a", "a", "a"],
                       "key_b": ["value_one", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a", "a"]
                    }"""
            )
        }
    }

    @Test
    fun `should throw exception when number is not in the accepted range`() {
        assertThrows<InvalidFilterPolicyException> {
            validator.validate("""{"a": [{"numeric": ["<", 2000000000.25] }]}""")
        }
    }

    @Test
    fun `should throw exception when numeric value is not a number`() {
        assertThrows<InvalidFilterPolicyException> {
            validator.validate("""{"a": [{"numeric": ["<", "2000000000.25"] }]}""")
        }
    }
}