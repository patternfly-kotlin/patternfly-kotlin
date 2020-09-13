package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfMain(classes: String? = null, content: Main.() -> Unit = {}): Main =
    register(Main(classes), content)

// ------------------------------------------------------ tag

class Main internal constructor(classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("main", baseClass = classes(ComponentType.Main, classes)) {

    init {
        markAs(ComponentType.Main)
        attr("role", "main")
        attr("tabindex", "-1")
    }
}
