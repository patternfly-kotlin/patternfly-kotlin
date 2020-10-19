package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun HtmlElements.pfPage(
    id: String? = null,
    baseClass: String? = null,
    content: Page.() -> Unit = {}
): Page = register(Page(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Page internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Page, baseClass)) {
    init {
        markAs(ComponentType.Page)
    }
}
