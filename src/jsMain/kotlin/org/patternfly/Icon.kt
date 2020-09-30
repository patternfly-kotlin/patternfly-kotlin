package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfIcon(
    iconClass: String,
    id: String? = null,
    classes: String? = null,
    content: Icon.() -> Unit = {}
): Icon = register(Icon(iconClass, id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Icon internal constructor(iconClass: String, id: String?, classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("i", id = id, baseClass = classes {
        +ComponentType.Icon
        +iconClass
        +classes
    }) {
    init {
        markAs(ComponentType.Icon)
        attr("aria-hidden", "true")
    }
}
