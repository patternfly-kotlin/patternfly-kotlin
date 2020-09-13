package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfContent(classes: String? = null, content: Content.() -> Unit = {}): Content =
    register(Content(classes), content)

// ------------------------------------------------------ tag

class Content internal constructor(classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.Content, classes)) {
    init {
        markAs(ComponentType.Content)
    }
}
