package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.HtmlElements

// ------------------------------------------------------ dsl

fun HtmlElements.pfTitle(text: String = "", level: Int = 1, size: Size = Size.XL_2, content: H.() -> Unit = {}): H =
    register(H(level, baseClass = "${"title".component()} ${size.modifier}").apply {
        if (text.isNotBlank()) {
            +text
        }
    }, content)
