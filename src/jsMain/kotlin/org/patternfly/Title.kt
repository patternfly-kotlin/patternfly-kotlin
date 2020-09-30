package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLHeadingElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfTitle(
    level: Int = 1,
    size: Size = Size.XL_2,
    id: String? = null,
    classes: String? = null,
    content: Title.() -> Unit = {}
): Title = register(Title(level, size, id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Title internal constructor(level: Int = 1, size: Size = Size.XL_2, id: String?, classes: String?) :
    PatternFlyComponent<HTMLHeadingElement>,
    H(level, id = id, baseClass = classes {
        +ComponentType.Title
        +size.modifier
        +classes
    }) {
    init {
        markAs(ComponentType.Title)
    }
}
