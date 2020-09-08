package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDivider(variant: DividerVariant = DividerVariant.HR, classes: String? = null): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR -> register(Hr(baseClass = classes("divider".component(), classes)), {})
        DividerVariant.DIV -> register(Div(baseClass = classes("divider".component(), classes)).apply {
            attr("role", "separator")
        }, {})
        DividerVariant.LI -> register(Li(baseClass = classes("divider".component(), classes)).apply {
            attr("role", "separator")
        }, {})
    }

fun HtmlElements.pfDivider(variant: DividerVariant = DividerVariant.HR, modifier: Modifier): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR -> register(Hr(baseClass = classes("divider".component(), modifier.value)), {})
        DividerVariant.DIV -> register(Div(baseClass = classes("divider".component(), modifier.value)).apply {
            attr("role", "separator")
        }, {})
        DividerVariant.LI -> register(Li(baseClass = classes("divider".component(), modifier.value)).apply {
            attr("role", "separator")
        }, {})
    }
