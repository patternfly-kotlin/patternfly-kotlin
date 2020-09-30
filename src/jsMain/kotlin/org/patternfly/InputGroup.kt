package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfInputGroup(
    id: String? = null,
    baseClass: String? = null,
    content: InputGroup.() -> Unit = {}
): InputGroup = register(InputGroup(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

class InputGroup internal constructor(id: String?, baseClass: String?) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.InputGroup, baseClass)) {
    init {
        markAs(ComponentType.InputGroup)
    }
}
