package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfContent(content: Content.() -> Unit = {}): Content =
    register(Content(), content)

// ------------------------------------------------------ tag

class Content internal constructor() :
    PatternFlyTag<HTMLDivElement>(ComponentType.Content, "div", "content".component()), Ouia
