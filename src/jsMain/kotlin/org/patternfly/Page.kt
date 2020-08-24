package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements

// ------------------------------------------------------ tag

fun HtmlElements.pfPage(content: Page.() -> Unit = {}): Page = register(Page(), content)

// ------------------------------------------------------ tag

class Page internal constructor() : Div(baseClass = "page".component()) {
    init {
        domNode.componentType(ComponentType.Page)
    }
}
