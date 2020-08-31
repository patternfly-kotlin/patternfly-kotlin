package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfIcon(iconClass: String, content: Icon.() -> Unit = {}): Icon =
    register(Icon(iconClass), content)

// ------------------------------------------------------ tag

class Icon internal constructor(iconClass: String) :
    PatternFlyComponent<HTMLElement>,
    TextElement("i", baseClass = classes(ComponentType.Icon, iconClass)) {
    init {
        markAs(ComponentType.Icon)
        attr("aria-hidden", "true")
    }
}
