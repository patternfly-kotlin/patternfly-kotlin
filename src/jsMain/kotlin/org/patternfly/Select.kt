package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfSelect(classes: String? = null, content: Select.() -> Unit = {}): Select =
    register(Select(classes), content)

fun HtmlElements.pfSelect(iconClass: String, modifier: Modifier, content: Select.() -> Unit = {}): Select =
    register(Select(modifier.value), content)

// ------------------------------------------------------ tag

class Select internal constructor(classes: String?) :
    PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
        +ComponentType.Select
        +classes
    }) {
    init {
        markAs(ComponentType.Select)
    }
}
