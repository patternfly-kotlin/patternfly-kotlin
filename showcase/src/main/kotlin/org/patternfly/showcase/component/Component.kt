package org.patternfly.showcase.component

import dev.fritz2.dom.html.HtmlElements
import org.patternfly.Section
import org.patternfly.pfContent
import org.patternfly.pfSection
import org.patternfly.pfTitle

fun HtmlElements.component(title: String, content: HtmlElements.() -> Unit): Section = pfSection {
    pfContent {
        pfTitle(title)
        content(this)
    }
}
