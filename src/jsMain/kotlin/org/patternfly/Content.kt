package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfContent(
    id: String? = null,
    classes: String? = null,
    content: Content.() -> Unit = {}
): Content = register(Content(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Content internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Content, classes)) {
    init {
        markAs(ComponentType.Content)
    }
}
