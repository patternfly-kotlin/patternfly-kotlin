package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfInputGroup(
    id: String? = null,
    classes: String? = null,
    content: InputGroup.() -> Unit = {}
): InputGroup = register(InputGroup(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class InputGroup internal constructor(id: String?, classes: String?) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.InputGroup, classes)) {
    init {
        markAs(ComponentType.InputGroup)
    }
}
