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
    id: String? = null,
    baseClass: String? = null
): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR ->
            hr(
                id = id,
                baseClass = classes {
                    +"divider".component()
                    +("vertical".modifier() `when` vertical)
                    +baseClass
                }
            ) {}
        DividerVariant.DIV ->
            div(
                id = id,
                baseClass = classes {
                    +"divider".component()
                    +("vertical".modifier() `when` vertical)
                    +baseClass
                }
            ) {
                attr("role", "separator")
            }
        DividerVariant.LI ->
            li(
                id = id,
                baseClass = classes {
                    +"divider".component()
                    +("vertical".modifier() `when` vertical)
                    +baseClass
                }
            ) {
                attr("role", "separator")
            }
    }
