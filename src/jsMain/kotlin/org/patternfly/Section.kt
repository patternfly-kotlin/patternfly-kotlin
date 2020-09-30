package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfSection(
    id: String? = null,
    classes: String? = null,
    content: Section.() -> Unit = {}
): Section = register(Section(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Section internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("section", id = id, baseClass = classes(ComponentType.Section, classes)) {

    init {
        markAs(ComponentType.Section)
    }
}
