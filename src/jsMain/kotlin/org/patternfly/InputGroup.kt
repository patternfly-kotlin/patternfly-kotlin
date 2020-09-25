package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfInputGroup(
    classes: String? = null,
    content: InputGroup.() -> Unit = {}
): InputGroup = register(InputGroup(classes), content)

// ------------------------------------------------------ tag

class InputGroup internal constructor(
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.InputGroup, classes)) {
    init {
        markAs(ComponentType.InputGroup)
    }
}
