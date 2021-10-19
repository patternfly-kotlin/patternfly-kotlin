package org.patternfly

import dev.fritz2.dom.Tag
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

/**
 * Creates a CSS class for screen-reader
 * The content is invisible and only available to a screen reader, use inspector to investigate
 *
 * @see <a href="https://www.patternfly.org/v4/utilities/accessibility/#screen-reader-only">
 *     https://www.patternfly.org/v4/utilities/accessibility/#screen-reader-only</a>
 */
public fun screenReader(): String = "screen-reader".util()

public class AriaContext {
    private var role: String? = null
    private val properties: MutableMap<String, String> = mutableMapOf()

    public fun role(role: String) {
        this.role = role
    }

    /**
     * Sets an ARIA property. See https://www.w3.org/TR/wai-aria-1.1/#state_prop_def for all properties and states.
     */
    public fun set(name: String, value: String) {
        properties[name] = value
    }

    internal fun <T : Tag<HTMLElement>> applyTo(tag: T) {
        role?.let {
            tag.aria["role"] = it
        }
        properties.forEach { (key, value) -> tag.aria[key] = value }
    }
}

public interface WithAria {
    public val ariaContext: AriaContext

    public fun aria(build: AriaContext.() -> Unit)
}

internal class AriaMixin : WithAria {
    override val ariaContext: AriaContext = AriaContext()

    override fun aria(build: AriaContext.() -> Unit) {
        ariaContext.apply(build)
    }
}

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
