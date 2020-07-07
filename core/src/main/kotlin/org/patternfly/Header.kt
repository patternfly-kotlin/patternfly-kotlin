package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun Header.pfBrand(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "page".component("header", "brand")), content)

fun HtmlElements.pfBrandLink(homeLink: String, content: A.() -> Unit = {}): A =
    register(A(baseClass = "page".component("header", "brand", "link")).apply {
        href = const(homeLink)
    }, content)

fun Page.pfHeader(content: Header.() -> Unit = {}): Header =
    register(Header(), content)

fun Header.pfHeaderTools(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "page".component("header", "tools")), content)

// ------------------------------------------------------ tag

class Header internal constructor() :
    PatternFlyTag<HTMLElement>(ComponentType.Header, "header", "page".component("header")), Ouia {
    init {
        attr("role", "banner")
    }
}
