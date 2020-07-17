package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfIcon(iconClass: String, content: Icon.() -> Unit = {}): Icon =
    register(Icon(iconClass), content)

// ------------------------------------------------------ tag

class Icon internal constructor(iconClass: String) :
    PatternFlyTag<HTMLElement>(ComponentType.Icon, "i", iconClass), Ouia {
    init {
        attr("aria-hidden", "true")
    }
}
