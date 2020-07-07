package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfMain(content: Main.() -> Unit = {}): Main = register(Main(), content)

// ------------------------------------------------------ tag

class Main internal constructor() :
    PatternFlyTag<HTMLElement>(ComponentType.Main, "main", "page".component("main")), Ouia {
    init {
        attr("role", "main")
        attr("tabindex", "-1")
    }
}
