package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfIcon(iconClass: String, content: Icon.() -> Unit = {}): Icon =
    register(Icon(iconClass), content)

// ------------------------------------------------------ tag

class Icon internal constructor(iconClass: String) : TextElement("i", baseClass = iconClass) {
    init {
        domNode.componentType(ComponentType.Icon)
        attr("aria-hidden", "true")
    }
}
