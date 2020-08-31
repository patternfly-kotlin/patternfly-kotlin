package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLHeadingElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfTitle(
    level: Int = 1,
    size: Size = Size.XL_2,
    classes: String? = null,
    content: Title.() -> Unit = {}
): Title = register(Title(level, size, classes), content)

fun HtmlElements.pfTitle(
    level: Int = 1,
    size: Size = Size.XL_2,
    modifier: Modifier,
    content: Title.() -> Unit = {}
): Title = register(Title(level, size, modifier.value), content)

// ------------------------------------------------------ tag

class Title internal constructor(level: Int = 1, size: Size = Size.XL_2, classes: String?) :
    PatternFlyComponent<HTMLHeadingElement>,
    H(level, baseClass = classes {
        +ComponentType.Title
        +size.modifier
        +classes
    }) {
    init {
        markAs(ComponentType.Title)
    }
}
