package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfSection(
    id: String? = null,
    baseClass: String? = null,
    content: Section.() -> Unit = {}
): Section = register(Section(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Section internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLElement>,
    TextElement("section", id = id, baseClass = classes(ComponentType.Section, baseClass)) {

    init {
        markAs(ComponentType.Section)
    }
}
