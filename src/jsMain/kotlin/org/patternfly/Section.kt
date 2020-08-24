package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfSection(content: Section.() -> Unit = {}): Section =
    register(Section(emptyList()), content)

fun HtmlElements.pfSection(vararg classes: String, content: Section.() -> Unit = {}): Section =
    register(Section(classes.toList()), content)

fun HtmlElements.pfSection(vararg modifier: Modifier, content: Section.() -> Unit = {}): Section =
    register(Section(modifier.map { it.value }), content)

// ------------------------------------------------------ tag

class Section internal constructor(classes: List<String>) :
    TextElement("section", baseClass = buildString {
        append("page".component("main-section"))
        if (classes.isNotEmpty()) {
            classes.joinTo(this, " ", " ")
        }
    }) {
    init {
        domNode.componentType(ComponentType.Section)
    }
}
