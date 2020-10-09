package org.patternfly

import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.render
import org.w3c.dom.DOMTokenList
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.ParentNode
import org.w3c.dom.asList
import kotlin.math.floor

// ------------------------------------------------------ token list

operator fun DOMTokenList.plusAssign(value: String) {
    this.add(value)
}

operator fun DOMTokenList.minusAssign(value: String) {
    this.remove(value)
}

// ------------------------------------------------------ parent / child

fun Element.appendAll(elements: Elements) {
    elements.forEach { this.appendChild(it) }
}

fun Node?.removeFromParent() {
    if (this != null && this.parentNode != null) {
        this.parentNode!!.removeChild(this)
    }
}

fun elements(content: HtmlElements.() -> Unit): List<Element> = render {
    div { content(this) }
}.domNode.childNodes.asList().map { it.unsafeCast<Element>() }

interface Elements : Iterable<Element> {
    val elements: List<Element>
    override fun iterator(): Iterator<Element> = elements.iterator()
}

// ------------------------------------------------------ aria

val WithDomNode<Element>.aria: Aria
    get() = Aria(this.domNode)

val Element.aria: Aria
    get() = Aria(this)

class Aria(private val element: Element) {

    operator fun contains(name: String): Boolean = element.hasAttribute(name)

    operator fun get(name: String): String = element.getAttribute(attributeSafeKey(name)) ?: ""

    operator fun set(name: String, value: Any) {
        element.setAttribute(attributeSafeKey(name), value.toString())
    }

    private fun attributeSafeKey(name: String) =
        if (name.startsWith("aria-")) name else "aria-$name"
}

// ------------------------------------------------------ visibility

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
    this.unsafeCast<HTMLElement>().style.display = ""
}

fun Element?.isInView(container: Element?, partial: Boolean = false): Boolean {
    if (this != null && container != null) {
        val containerBounds = container.getBoundingClientRect()
        val elementBounds = this.getBoundingClientRect()
        val containerBoundsLeft = floor(containerBounds.left)
        val containerBoundsRight = floor(containerBounds.right)
        val elementBoundsLeft = floor(elementBounds.left)
        val elementBoundsRight = floor(elementBounds.right)

        // Check if in view
        val totallyInView = elementBoundsLeft >= containerBoundsLeft && elementBoundsRight <= containerBoundsRight;
        val partiallyInView = partial &&
                ((elementBoundsLeft < containerBoundsLeft && elementBoundsRight > containerBoundsLeft) ||
                        (elementBoundsRight > containerBoundsRight && elementBoundsLeft < containerBoundsRight))

        // Return outcome
        return totallyInView || partiallyInView;
    } else {
        return false
    }
}

// ------------------------------------------------------ selector

fun Element.closest(selector: By): Element? = this.closest(selector.selector)

fun Element.matches(selector: By): Boolean = this.matches(selector.selector)

fun ParentNode.querySelector(selector: By) = this.querySelector(selector.selector)

fun ParentNode.querySelectorAll(selector: By) = this.querySelectorAll(selector.selector)

// ------------------------------------------------------ debug

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
