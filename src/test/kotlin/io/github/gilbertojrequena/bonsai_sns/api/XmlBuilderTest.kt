package io.github.gilbertojrequena.bonsai_sns.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
internal class XmlBuilderTest {

    @Test
    fun `should create xml with elements and attributes`() {
        val xml = xml("A") {
            element("B") {
                attribute("y", "z")
                attribute("w", "x")
                element("C")
            }
        }
        assertEquals(
            """<A><ResponseMetadata><RequestId>00000000-0000-0000-0000-000000000000</RequestId></ResponseMetadata><B y="z" w="x"><C /></B></A>""",
            xml
        )
    }

}