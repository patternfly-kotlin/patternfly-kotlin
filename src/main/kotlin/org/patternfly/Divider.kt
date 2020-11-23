package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfDivider(
    variant: DividerVariant = DividerVariant.HR,
    id: String? = null,
    baseClass: String? = null
): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR -> register(Hr(id = id, baseClass = classes("divider".component(), baseClass), job), {})
        DividerVariant.DIV -> register(Div(id = id, baseClass = classes("divider".component(), baseClass), job).apply {
            attr("role", "separator")
        }, {})
        DividerVariant.LI -> register(Li(id = id, baseClass = classes("divider".component(), baseClass), job).apply {
            attr("role", "separator")
        }, {})
    }
