package org.patternfly

import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.render
import org.w3c.dom.DOMTokenList
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ParentNode
import org.w3c.dom.asList
import kotlin.math.floor

// ------------------------------------------------------ token list

public operator fun DOMTokenList.plusAssign(value: String) {
    this.add(value)
}

public operator fun DOMTokenList.minusAssign(value: String) {
    this.remove(value)
}

// ------------------------------------------------------ parent / child

public fun Element.appendAll(elements: Elements) {
    elements.forEach { this.appendChild(it) }
}

public fun Node?.removeFromParent() {
    if (this != null && this.parentNode != null) {
        this.parentNode!!.removeChild(this)
    }
}

public fun elements(content: HtmlElements.() -> Unit): List<Element> = render {
    div { content(this) }
}.domNode.childNodes.asList().map { it.unsafeCast<Element>() }

public interface Elements : Iterable<Element> {
    public val elements: List<Element>
    public override fun iterator(): Iterator<Element> = elements.iterator()
}

// ------------------------------------------------------ aria

public val WithDomNode<Element>.aria: Aria
    get() = Aria(this.domNode)

public val Element.aria: Aria
    get() = Aria(this)

public class Aria(private val element: Element) {

    public operator fun contains(name: String): Boolean = element.hasAttribute(name)

    public operator fun get(name: String): String = element.getAttribute(attributeSafeKey(name)) ?: ""

    public operator fun set(name: String, value: Any) {
        element.setAttribute(attributeSafeKey(name), value.toString())
    }

    private fun attributeSafeKey(name: String) =
        if (name.startsWith("aria-")) name else "aria-$name"
}

// ------------------------------------------------------ visibility

public var Element.hidden: Boolean
    get() = getAttribute("hidden")?.toBoolean() ?: false
    set(value) {
        setAttribute("hidden", value.toString())
    }

public var Element.styleHidden: Boolean
    get() = this.unsafeCast<HTMLElement>().style.display != "none"
    set(value) {
        if (value) {
            hide()
        } else {
            show()
        }
    }

public fun Element.hide() {
    this.unsafeCast<HTMLElement>().style.display = "none"
}

public fun Element.show() {
    this.unsafeCast<HTMLElement>().style.display = ""
}

public fun Element?.isInView(container: Element?, partial: Boolean = false): Boolean {
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

public fun Element.closest(selector: By): Element? = this.closest(selector.selector)

public fun Element.matches(selector: By): Boolean = this.matches(selector.selector)

public fun ParentNode.querySelector(selector: By): Element? = this.querySelector(selector.selector)

public fun ParentNode.querySelectorAll(selector: By): NodeList = this.querySelectorAll(selector.selector)

// ------------------------------------------------------ debug

public fun Element.debug(): String = buildString {
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
