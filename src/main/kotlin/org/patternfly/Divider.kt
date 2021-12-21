package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates a PatternFly [divider](https://www.patternfly.org/v4/components/divider/design-guidelines) component.
 *
 * A divider is a horizontal separator that creates space or groups items within a page or list.
 *
 * @sample org.patternfly.sample.DividerSample.divider
 */
public fun RenderContext.divider(
    variant: DividerVariant = DividerVariant.HR,
    vertical: Boolean = false,
    baseClass: String? = null,
    id: String? = null
): Tag<HTMLElement> = when (variant) {
    DividerVariant.HR ->
        hr(
            baseClass = classes {
                +"divider".component()
                +("vertical".modifier() `when` vertical)
                +baseClass
            },
            id = id
        ) {}
    DividerVariant.DIV ->
        div(
            baseClass = classes {
                +"divider".component()
                +("vertical".modifier() `when` vertical)
                +baseClass
            },
            id = id
        ) {
            attr("role", "separator")
        }
    DividerVariant.LI ->
        li(
            baseClass = classes {
                +"divider".component()
                +("vertical".modifier() `when` vertical)
                +baseClass
            },
            id = id
        ) {
            attr("role", "separator")
        }
}

// ------------------------------------------------------ component

/**
 * Visual modifier for [divider]s.
 */
public enum class DividerVariant {
    HR, DIV, LI
}
