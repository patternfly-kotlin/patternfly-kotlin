package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ tag

fun HtmlElements.pfPage(content: Page.() -> Unit = {}): Page = register(Page(), content)

// ------------------------------------------------------ tag

class Page internal constructor() :
    PatternFlyTag<HTMLDivElement>(ComponentType.Page, "div", "page".component()), Ouia
