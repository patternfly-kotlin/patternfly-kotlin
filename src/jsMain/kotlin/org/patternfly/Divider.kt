package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Hr
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDivider(variant: DividerVariant = DividerVariant.HR, baseClass: String? = null): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR -> register(Hr(baseClass = classes("divider".component(), baseClass)), {})
        DividerVariant.DIV -> register(Div(baseClass = classes("divider".component(), baseClass)).apply {
            attr("role", "separator")
        }, {})
        DividerVariant.LI -> register(Div(baseClass = classes("divider".component(), baseClass)).apply {
            attr("role", "separator")
        }, {})
    }

fun HtmlElements.pfDivider(variant: DividerVariant = DividerVariant.HR, modifier: Modifier): Tag<HTMLElement> =
    when (variant) {
        DividerVariant.HR -> register(Hr(baseClass = classes("divider".component(), modifier.value)), {})
        DividerVariant.DIV -> register(Div(baseClass = classes("divider".component(), modifier.value)).apply {
            attr("role", "separator")
        }, {})
        DividerVariant.LI -> register(Div(baseClass = classes("divider".component(), modifier.value)).apply {
            attr("role", "separator")
        }, {})
    }
