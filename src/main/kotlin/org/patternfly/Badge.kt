package org.patternfly

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.dom.minusAssign
import org.patternfly.dom.plusAssign
import org.w3c.dom.HTMLSpanElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Badge] component.
 *
 * @param min the minimum number to show in the badge
 * @param max the maximum number to show in the badge
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.badge(
    min: Int = 0,
    max: Int = 999,
    id: String? = null,
    baseClass: String? = null,
    content: Badge.() -> Unit = {}
): Badge = register(Badge(min, max, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [badge](https://www.patternfly.org/v4/components/badge/design-guidelines) component.
 *
 * A badge is used to annotate other information like a label or an object name.
 *
 * If the badge value is numeric, the value is adjusted so that it is within the bounds of [min] and [max].
 *
 * @sample org.patternfly.sample.BadgeSample.badge
 */
public class Badge internal constructor(
    private val min: Int,
    private val max: Int,
    id: String? = null,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLSpanElement>, Span(id = id, baseClass = classes(ComponentType.Badge, baseClass), job) {

    init {
        markAs(ComponentType.Badge)
    }

    /** Marks the badge as (un)read. */
    public fun read(value: Boolean) {
        if (value) {
            domNode.classList += "read".modifier()
            domNode.classList -= "unread".modifier()
        } else {
            domNode.classList += "unread".modifier()
            domNode.classList -= "read".modifier()
        }
    }

    /** Marks the badge as (un)read. */
    public fun read(value: Flow<Boolean>) {
        classMap(value.map { mapOf("read".modifier() to it, "unread".modifier() to !it) })
    }

    /**
     * Sets the value of this badge. If the value is numeric, it is adjusted so that it is within the bounds of
     * [min] and [max].
     */
    public fun value(value: Flow<String>) {
        mountSingle(job, value) { v, _ -> value(v) }
    }

    /**
     * Sets the value of this badge. If the value is numeric, it is adjusted so that it is within the bounds of
     * [min] and [max].
     */
    public fun value(value: String) {
        val numeric = value.toIntOrNull()
        val text = if (numeric != null) {
            applyBounds(numeric)
        } else value
        this.domNode.textContent = text
    }

    /** Sets the value of this badge. The value is adjusted so that it is within the bounds of [min] and [max]. */
    public fun value(value: Flow<Int>) {
        mountSingle(job, value) { v, _ -> value(v) }
    }

    /** Sets the value of this badge. The value is adjusted so that it is within the bounds of [min] and [max]. */
    public fun value(value: Int) {
        this.domNode.textContent = applyBounds(value)
    }

    private fun applyBounds(value: Int): String = when {
        value < min -> "<${min}"
        value > max -> "${max}+"
        else -> value.toString()
    }
}
