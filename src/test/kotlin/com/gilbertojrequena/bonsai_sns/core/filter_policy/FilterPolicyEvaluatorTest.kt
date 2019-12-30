package com.gilbertojrequena.bonsai_sns.core.filter_policy

import com.gilbertojrequena.bonsai_sns.core.MessageAttribute
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class FilterPolicyEvaluatorTest {
    private val evaluator = FilterPolicyEvaluator()

    @Test
    fun `should evaluate to true for matching string value`() {
        assertTrue(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "rugby")),
                """{"customer_interests": "rugby"}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for non-matching string value`() {
        assertFalse(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "rugby")),
                """{"customer_interests": "football"}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive or operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "rugby")),
                """{"customer_interests": ["rugby", "movies"]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative or operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "electronics")),
                """{"customer_interests": ["rugby", "movies"]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "electronics")),
                """{"customer_interests": [{"anything-but": ["rugby", "tennis"]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with array`() {
        assertTrue(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String.Array", """["electronics", "rugby"]""")),
                """{"customer_interests": [{"anything-but": ["rugby", "tennis"]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with number array`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number.Array", """[100, 50]""")),
                """{"price": [{"anything-but": [100, 500]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with number array with null policy value`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number.Array", """[100, 50]""")),
                """{"price": [{"anything-but": [100, null]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with boolean array`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("String.Array", """[false]""")),
                """{"price": [{"anything-but": [true, true]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with boolean array with null policy value`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("String.Array", """[false]""")),
                """{"price": [{"anything-but": [true, null]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with boolean array with null attribute value`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("String.Array", """[null, false]""")),
                """{"price": [{"anything-but": [true]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive anything-but operation with number array with null attribute value`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number.Array", """[100, null]""")),
                """{"price": [{"anything-but": [100, 500]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative anything-but operation with array`() {
        assertFalse(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String.Array", """["tennis", "rugby"]""")),
                """{"customer_interests": [{"anything-but": ["rugby", "tennis"]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative anything-but operation with number array`() {
        assertFalse(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number.Array", """[11.23456, 22.12]""")),
                """{"price": [{"anything-but": [11.23456, 22.12]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative anything-but operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "electronics")),
                """{"customer_interests": [{"anything-but": ["electronics", "tennis"]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive numeric anything-but operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "100.21")),
                """{"price": [{"anything-but": [100, 25.5]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative numeric anything-but operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "100")),
                """{"price": [{"anything-but": [30, 100]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive prefix operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "electronics")),
                """{"customer_interests": [{"prefix": "elec"}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative prefix operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("customer_interests" to MessageAttribute("String", "electronics")),
                """{"customer_interests": [{"prefix": "mus"}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive numeric equality operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "3.015e2")),
                """{"price": [{"numeric": ["=", 301.5]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative numeric equality operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "-3.015e2")),
                """{"price": [{"numeric": ["=", 301.5]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive numeric range operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "3.025e2")),
                """{"price": [{"numeric": [">=", 301.5]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative numeric range operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "-3.015e2")),
                """{"price": [{"numeric": [">", 0]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive numeric complex range operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "3.015e2")),
                """{"price": [{"numeric": [">", 0, "<=", 1500]}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for positive exist operation`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "-3.015e2")),
                """{"price": [{"exists": true}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false when expecting attribute to not exist but the attribute is present`() {
        assertFalse(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "-3.015e2")),
                """{"price": [{"exists": false}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true when expecting attribute to not exist and it is not present`() {
        assertTrue(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "3.015e2")),
                """{"week": [{"exists": false}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to false for negative exist operation`() {
        assertFalse(
            evaluator.eval(
                mapOf("price" to MessageAttribute("Number", "3.015e2")),
                """{"week": [{"exists": true}]}"""
            )
        )
    }

    @Test
    fun `should evaluate to true for policy that accepts message`() {
        assertTrue(
            evaluator.eval(
                mapOf(
                    "customer_interests" to MessageAttribute("String", "rugby"),
                    "store" to MessageAttribute("String", "example_corp"),
                    "event" to MessageAttribute("String", "order_placed"),
                    "price_usd" to MessageAttribute("Number", "210.75")
                ),
                """{
                           "store": ["example_corp"],
                           "event": [{"anything-but": "order_cancelled"}],
                           "customer_interests": [
                              "rugby",
                              "football",
                              "baseball"
                           ],
                           "price_usd": [{"numeric": [">=", 100]}]
                        }"""
            )
        )
    }

    @Test
    fun `should evaluate to false for policy that rejects message`() {
        assertFalse(
            evaluator.eval(
                mapOf(
                    "customer_interests" to MessageAttribute("String", "rugby"),
                    "store" to MessageAttribute("String", "example_corp"),
                    "event" to MessageAttribute("String", "order_cancelled"),
                    "price_usd" to MessageAttribute("Number", "210.75")
                ),
                """{
                           "store": ["example_corp"],
                           "event": ["order_cancelled"],
                           "encrypted": [false],
                           "customer_interests": [
                              "basketball",
                              "baseball"
                           ]
                        }"""
            )
        )
    }
}