package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun HtmlElements.pfSelect(
    id: String? = null,
    baseClass: String? = null,
    content: Select.() -> Unit = {}
): Select = register(Select(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Select internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.Select, baseClass)) {
    init {
        markAs(ComponentType.Select)
    }
}
