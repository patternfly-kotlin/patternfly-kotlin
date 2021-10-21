package org.patternfly

import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLSpanElement

// ------------------------------------------------------ factory

/**
 * Creates a [Badge] component.
 *
 * @param count the number displayed on this badge
 * @param min the minimum number displayed on this badge
 * @param max the maximum number displayed on this badge
 * @param read whether this badge is marked read or unread
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.BadgeSample.badge
 */
public fun RenderContext.badge(
    count: Int = 0,
    min: Int = Badge.BADGE_MIN,
    max: Int = Badge.BADGE_MAX,
    read: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    build: Badge.() -> Unit = {}
) {
    Badge(count, min, max, read).apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [badge](https://www.patternfly.org/v4/components/badge/design-guidelines) component.
 *
 * A badge is used to annotate other information like a label or an object name.
 *
 * If the badge value is numeric, the value is adjusted so that it is within the bounds of [min] and [max].
 *
 * @sample org.patternfly.sample.BadgeSample.badge
 */
public class Badge internal constructor(count: Int, private var min: Int, private var max: Int, read: Boolean) :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Span, HTMLSpanElement> by ElementMixin(),
    WithEvents<HTMLSpanElement> by EventMixin() {

    private var read: Flow<Boolean> = flowOf(read)
    private var count: Flow<Int> = flowOf(count)

    public fun min(min: Int) {
        this.min = min
    }

    public fun max(max: Int) {
        this.max = max
    }

    public fun bounds(min: Int, max: Int) {
        this.min = min
        this.max = max
    }

    public fun bounds(bounds: IntRange) {
        this.min = bounds.first
        this.max = bounds.last
    }

    public fun read(read: Boolean) {
        this.read = flowOf(read)
    }

    public fun read(read: Flow<Boolean>) {
        this.read = read
    }

    public fun count(count: Int) {
        this.count = flowOf(count)
    }

    public fun count(count: Flow<Int>) {
        this.count = count
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            span(baseClass = classes(ComponentType.Badge, baseClass), id = id) {
                markAs(ComponentType.Badge)
                aria(this)
                element(this)
                events(this)

                classMap(read.map { mapOf("read".modifier() to it, "unread".modifier() to !it) })
                count.map { applyBounds(it) }.asText()
            }
        }
    }

    private fun applyBounds(value: Int): String = when {
        value < min -> "<$min"
        value > max -> "$max+"
        else -> value.toString()
    }

    public companion object {
        public const val BADGE_MIN: Int = 0
        public const val BADGE_MAX: Int = 999
    }
}
