package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

public fun Page.pfHeader(
    id: String? = null,
    baseClass: String? = null,
    content: Header.() -> Unit = {}
): Header = register(Header(id = id, baseClass = baseClass), content)

public fun Header.pfHeaderTools(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("page".component("header", "tools"), baseClass)), content)

// ------------------------------------------------------ tag

public class Header internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("header", id = id, baseClass = classes(ComponentType.Header, baseClass)) {
    init {
        markAs(ComponentType.Header)
        attr("role", "banner")
    }
}
