package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDivider(
    variant: DividerVariant = DividerVariant.HR,
    id: String? = null,
    classes: String? = null
): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR -> register(Hr(id = id, baseClass = classes("divider".component(), classes)), {})
        DividerVariant.DIV -> register(Div(id = id, baseClass = classes("divider".component(), classes)).apply {
            attr("role", "separator")
        }, {})
        DividerVariant.LI -> register(Li(id = id, baseClass = classes("divider".component(), classes)).apply {
            attr("role", "separator")
        }, {})
    }
