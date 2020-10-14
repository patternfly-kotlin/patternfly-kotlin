package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun HtmlElements.pfContent(
    id: String? = null,
    baseClass: String? = null,
    content: Content.() -> Unit = {}
): Content = register(Content(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Content internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Content, baseClass)) {
    init {
        markAs(ComponentType.Content)
    }
}
