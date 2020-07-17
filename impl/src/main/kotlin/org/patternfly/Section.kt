package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfSection(vararg classes: String, content: Section.() -> Unit = {}): Section =
    register(Section(*classes), content)

// ------------------------------------------------------ tag

class Section internal constructor(vararg classes: String) :
    PatternFlyTag<HTMLElement>(
        ComponentType.Section,
        "section",
        "page".component("main-section").append(*classes)
    ), Ouia
