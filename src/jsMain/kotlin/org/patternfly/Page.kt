package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ tag

fun HtmlElements.pfPage(classes: String? = null, content: Page.() -> Unit = {}): Page =
    register(Page(classes), content)

// ------------------------------------------------------ tag

class Page internal constructor(classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.Page, classes)) {
    init {
        markAs(ComponentType.Page)
    }
}
