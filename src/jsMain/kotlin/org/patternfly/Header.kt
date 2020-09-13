package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun Page.pfHeader(classes: String? = null, content: Header.() -> Unit = {}): Header =
    register(Header(classes), content)

fun Header.pfHeaderTools(classes: String? = null, content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = classes("page".component("header", "tools"), classes)), content)

// ------------------------------------------------------ tag

class Header internal constructor(classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("header", baseClass = classes(ComponentType.Header, classes)) {

    init {
        markAs(ComponentType.Header)
        attr("role", "banner")
    }
}
