package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfSection(classes: String? = null, content: Section.() -> Unit = {}): Section =
    register(Section(classes), content)

fun HtmlElements.pfSection(modifier: Modifier, content: Section.() -> Unit = {}): Section =
    register(Section(modifier.value), content)

// ------------------------------------------------------ tag

class Section internal constructor(classes: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("section", baseClass = classes(ComponentType.Section, classes)) {

    init {
        markAs(ComponentType.Section)
    }
}
