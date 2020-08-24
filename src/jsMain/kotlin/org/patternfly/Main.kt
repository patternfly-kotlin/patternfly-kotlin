package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfMain(content: Main.() -> Unit = {}): Main = register(Main(), content)

// ------------------------------------------------------ tag

class Main internal constructor() :
    TextElement("main", baseClass = "page".component("main")) {
    init {
        domNode.componentType(ComponentType.Main)
        attr("role", "main")
        attr("tabindex", "-1")
    }
}
