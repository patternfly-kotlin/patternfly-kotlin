package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfIcon(
    iconClass: String,
    id: String? = null,
    baseClass: String? = null,
    content: Icon.() -> Unit = {}
): Icon = register(Icon(iconClass, id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

class Icon internal constructor(iconClass: String, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("i", id = id, baseClass = classes {
        +ComponentType.Icon
        +iconClass
        +baseClass
    }) {
    init {
        markAs(ComponentType.Icon)
        attr("aria-hidden", "true")
    }
}
