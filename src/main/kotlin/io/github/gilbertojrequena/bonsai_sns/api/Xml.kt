package io.github.gilbertojrequena.bonsai_sns.api

import org.jdom2.Attribute
import org.jdom2.Namespace
import org.jdom2.output.XMLOutputter

private val xmlOutputter = XMLOutputter()

class Element(name: String, namespace: Namespace = Namespace.NO_NAMESPACE) : org.jdom2.Element(name, namespace) {

    fun element(name: String, init: Element.() -> Unit = {}): Element {
        val element = Element(name)
        element.init()
        children.add(0, element)
        return element
    }

    fun attribute(name: String, value: String) {
        val att = Attribute(name, value)
        attributes.add(att)
    }
}


fun xml(
    name: String,
    requestId: String = "00000000-0000-0000-0000-000000000000",
    init: Element.() -> Unit = {}
): String {
    val xml = Element(name)
    xml.init()
    xml.element("ResponseMetadata") {
        element("RequestId") {
            text = requestId
        }
    }
    return xmlOutputter.outputString(xml)
}