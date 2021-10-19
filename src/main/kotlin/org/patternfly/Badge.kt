package org.patternfly

import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// ------------------------------------------------------ factory

/**
 * Creates a [Badge] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.BadgeSample.badge
 */
public fun RenderContext.badge(
    baseClass: String? = null,
    id: String? = null,
    build: Badge.() -> Unit
) {
    Badge().apply(build).render(this, baseClass, id)
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
public class Badge : PatternFlyComponent<Unit> {

    private var min: Int = BADGE_MIN
    private var max: Int = BADGE_MAX
    private var read: Flow<Boolean> = flowOf(false)
    private var count: Flow<Int> = flowOf(0)

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
