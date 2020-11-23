package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfMain(
    id: String? = null,
    baseClass: String? = null,
    content: Main.() -> Unit = {}
): Main = register(Main(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Main internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("main", id = id, baseClass = classes(ComponentType.Main, baseClass)) {
    init {
        markAs(ComponentType.Main)
        attr("role", "main")
        attr("tabindex", "-1")
    }
}
