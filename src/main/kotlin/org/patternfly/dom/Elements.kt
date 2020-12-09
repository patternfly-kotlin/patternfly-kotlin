package org.patternfly.dom

import dev.fritz2.dom.Tag
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.DOMTokenList
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.ParentNode
import kotlin.math.floor

// ------------------------------------------------------ token list

/**
 * Adds the specified class to the list of CSS classes.
 */
public operator fun DOMTokenList.plusAssign(value: String) {
    this.add(value)
}

/**
 * Removes the specified class to the list of CSS classes.
 */
public operator fun DOMTokenList.minusAssign(value: String) {
    this.remove(value)
}

// ------------------------------------------------------ parent / child

/**
 * Appends all tags to this element.
 */
public fun <E : Element> Element.appendAll(elements: List<Tag<E>>) {
    elements.forEach { this.appendChild(it.domNode) }
}

/**
 * Appends all elements to this element.
 */
public fun Element.appendAll(elements: List<Element>) {
    elements.forEach { this.appendChild(it) }
}

/**
 * Removes this node from its parent (if any).
 */
public fun Node?.removeFromParent() {
    if (this != null && this.parentNode != null) {
        this.parentNode!!.removeChild(this)
    }
}

// ------------------------------------------------------ aria

/**
 * Getter for the [aria][TagAria] helper class.
 */
public val <E : Element> Tag<E>.aria: TagAria<E>
    get() = TagAria(this)

/**
 * Getter for the [aria][TagAria] helper class.
 */
public val Element.aria: ElementAria
    get() = ElementAria(this)

/**
 * Class that simplifies the handling of ARIA attributes on [Tag]s in a way that you don't have to specify the `aria-` prefix.
 *
 * @see ElementAria
 * @sample org.patternfly.sample.AriaSample.tagAria
 */
public class TagAria<E : Element>(private val tag: Tag<E>) : ElementAria(tag.domNode) {

    /**
     * Sets the values from the flow as values for the ARIA attribute.
     */
    public operator fun set(name: String, value: Flow<String>) {
        tag.attr(attributeSafeKey(name), value)
    }
}

/**
 * Class that simplifies the handling of ARIA attributes on [Element]s in a way that you don't have to specify the `aria-` prefix.
 *
 * @sample org.patternfly.sample.AriaSample.elementAria
 */
public open class ElementAria(private val element: Element) {

    /**
     * Tests whether the specified aria element is present on the [Element].
     */
    public operator fun contains(name: String): Boolean = element.hasAttribute(name)

    /**
     * Get the specified aria attribute or an empty string if the attribute is not present.
     */
    public operator fun get(name: String): String = element.getAttribute(attributeSafeKey(name)) ?: ""

    /**
     * Sets the specified aria attribute on the [Element].
     */
    public operator fun set(name: String, value: Any) {
        element.setAttribute(attributeSafeKey(name), value.toString())
    }

    internal fun attributeSafeKey(name: String) =
        if (name.startsWith("aria-")) name else "aria-$name"
}

// ------------------------------------------------------ visibility

/**
 * Property for the global HTML [`hidden`](http://developer.mozilla.org/en-US/docs/Web/HTML/Global_attributes/hidden) attribute.
 */
public var Element.hidden: Boolean
    get() = getAttribute("hidden")?.toBoolean() ?: false
    set(value) {
        if (value) {
            setAttribute("hidden", "")
        } else {
            removeAttribute("hidden")
        }
    }

/**
 * Property for the CSS [`display`](http://developer.mozilla.org/en-US/docs/Web/CSS/display) property.
 *
 * Setting this property to `true` sets the `display` property to `none` (same as calling [hide]). Setting this property to `false`, sets the `display` property to the empty string (same as calling [show]).
 */
public var Element.displayNone: Boolean
    get() = this.unsafeCast<HTMLElement>().style.display != "none"
    set(value) {
        if (value) {
            hide()
        } else {
            show()
        }
    }

/**
 * Sets the CSS [`display`](http://developer.mozilla.org/en-US/docs/Web/CSS/display) property to `none`.
 */
public fun Element.hide() {
    this.unsafeCast<HTMLElement>().style.display = "none"
}

/**
 * Sets the CSS [`display`](http://developer.mozilla.org/en-US/docs/Web/CSS/display) property to the empty string.
 */
public fun Element.show() {
    this.unsafeCast<HTMLElement>().style.display = ""
}

/**
 * Checks whether this element is (partially) in view of the specified container.
 */
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

/**
 * Calls [Element.closest] with the specified [By] selector.
 */
public fun Element.closest(selector: By): Element? = this.closest(selector.selector)

/**
 * Calls [Element.matches] with the specified [By] selector.
 */
public fun Element.matches(selector: By): Boolean = this.matches(selector.selector)

/**
 * Calls [ParentNode.querySelector] with the specified [By] selector.
 */
public fun ParentNode.querySelector(selector: By): Element? = this.querySelector(selector.selector)

/**
 * Calls [ParentNode.querySelectorAll] with the specified [By] selector.
 */
public fun ParentNode.querySelectorAll(selector: By): NodeList = this.querySelectorAll(selector.selector)

// ------------------------------------------------------ debug

/**
 * Convenience function to turn this element into a string.
 *
 * The string contains the element name and attributes (if any), but *no* child elements. However you can see whether the element has children in the way how the string is generated:
 *
 * - w/o children: `<element/>`
 * - w/ children: `<element></element>`
 *
 * @sample org.patternfly.sample.DebugSample.debug
 */
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
    if (hasChildNodes()) {
        append("></${tagName.toLowerCase()}")
    } else {
        append("/>")
    }
}
