package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLElement

fun HtmlElements.pfDivider(variant: DividerVariant = DividerVariant.HR): Tag<HTMLElement> = when (variant) {
    DividerVariant.HR -> register(Hr(baseClass = "divider".component()), {})
    DividerVariant.DIV -> register(Div(baseClass = "divider".component()).apply {
        attr("role", "separator")
    }, {})
    DividerVariant.LI -> register(Div(baseClass = "divider".component()).apply {
        attr("role", "separator")
    }, {})
}
