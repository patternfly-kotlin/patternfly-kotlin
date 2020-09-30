package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun Page.pfHeader(
    id: String? = null,
    classes: String? = null,
    content: Header.() -> Unit = {}
): Header = register(Header(id = id, classes = classes), content)

fun Header.pfHeaderTools(
    id: String? = null,
    classes: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("page".component("header", "tools"), classes)), content)

// ------------------------------------------------------ tag

class Header internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("header", id = id, baseClass = classes(ComponentType.Header, classes)) {

    init {
        markAs(ComponentType.Header)
        attr("role", "banner")
    }
}
