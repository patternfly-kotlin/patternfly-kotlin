package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ tag

fun HtmlElements.pfPage(
    id: String? = null,
    classes: String? = null,
    content: Page.() -> Unit = {}
): Page = register(Page(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Page internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Page, classes)) {
    init {
        markAs(ComponentType.Page)
    }
}
