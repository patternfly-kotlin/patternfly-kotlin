package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLHeadingElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfTitle(
    level: Int = 1,
    size: Size = Size.XL_2,
    id: String? = null,
    baseClass: String? = null,
    content: Title.() -> Unit = {}
): Title = register(Title(level, size, id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

class Title internal constructor(level: Int = 1, size: Size = Size.XL_2, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLHeadingElement>,
    H(level, id = id, baseClass = classes {
        +ComponentType.Title
        +size.modifier
        +baseClass
    }) {
    init {
        markAs(ComponentType.Title)
    }
}
