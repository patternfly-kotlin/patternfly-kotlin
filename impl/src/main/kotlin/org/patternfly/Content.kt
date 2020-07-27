package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements

// ------------------------------------------------------ dsl

fun HtmlElements.pfContent(content: Content.() -> Unit = {}): Content = register(Content(), content)

// ------------------------------------------------------ tag

class Content internal constructor() : Div(baseClass = "content".component()) {
    init {
        domNode.componentType(ComponentType.Content)
    }
}
