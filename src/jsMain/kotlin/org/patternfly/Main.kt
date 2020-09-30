package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfMain(
    id: String? = null,
    classes: String? = null,
    content: Main.() -> Unit = {}
): Main = register(Main(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Main internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("main", id = id, baseClass = classes(ComponentType.Main, classes)) {
    init {
        markAs(ComponentType.Main)
        attr("role", "main")
        attr("tabindex", "-1")
    }
}
