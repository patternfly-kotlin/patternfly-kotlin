package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfIcon(iconClass: String, classes: String? = null, content: Icon.() -> Unit = {}): Icon =
    register(Icon(iconClass, classes), content)

// ------------------------------------------------------ tag

class Icon internal constructor(iconClass: String, classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("i", baseClass = classes {
        +ComponentType.Icon
        +iconClass
        +classes
    }) {
    init {
        markAs(ComponentType.Icon)
        attr("aria-hidden", "true")
    }
}
