package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfSelect(
    id: String? = null,
    classes: String? = null,
    content: Select.() -> Unit = {}
): Select = register(Select(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Select internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.Select, classes)) {
    init {
        markAs(ComponentType.Select)
    }
}
