package org.patternfly

import dev.fritz2.dom.WithDomNode
import org.w3c.dom.DOMTokenList
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.ParentNode

// ------------------------------------------------------ token list

operator fun DOMTokenList.plusAssign(value: String) {
    this.add(value)
}

operator fun DOMTokenList.minusAssign(value: String) {
    this.remove(value)
}

// ------------------------------------------------------ node

fun Node?.removeFromParent() {
    if (this != null && this.parentNode != null) {
        this.parentNode!!.removeChild(this)
    }
}

// ------------------------------------------------------ element et al

val WithDomNode<Element>.aria: Aria
    get() = Aria(this.domNode)

val Element.aria: Aria
    get() = Aria(this)

var Element.hidden
    get() = getAttribute("hidden")?.toBoolean()
    set(value) {
        setAttribute("hidden", value.toString())
    }

var Element.styleHidden: Boolean
    get() = this.unsafeCast<HTMLElement>().style.display != "none"
    set(value) {
        if (value) {
            hide()
        } else {
            show()
        }
    }

fun Element.hide() {
    this.unsafeCast<HTMLElement>().style.display = "none"
}

fun Element.show() {
    this.unsafeCast<HTMLElement>().style.display = "unset"
}

fun Element.closest(selector: By): Element? = this.closest(selector.selector)

fun Element.matches(selector: By): Boolean = this.matches(selector.selector)

fun Element.debug(): String = buildString {
    append("<${tagName.toLowerCase()}")
    getAttributeNames().joinTo(this, " ", " ") { name ->
        buildString {
            append(name)
            getAttribute(name)?.let { value ->
                append("=\"$value\"")
            }
        }
    }
    append("/>")
}

fun ParentNode.querySelector(selector: By) = this.querySelector(selector.selector)

fun ParentNode.querySelectorAll(selector: By) = this.querySelectorAll(selector.selector)

class Aria(private val element: Element) {

    operator fun contains(name: String): Boolean = element.hasAttribute(name)

    operator fun get(name: String): String = element.getAttribute(attributeSafeKey(name)) ?: ""

    operator fun set(name: String, value: Any) {
        element.setAttribute(attributeSafeKey(name), value.toString())
    }

    private fun attributeSafeKey(name: String) =
        if (name.startsWith("aria-")) name else "aria-$name"
}
