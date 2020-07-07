package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfEmptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState =
    register(EmptyState(iconClass, title, size, content), {})

fun EmptyState.pfEmptyStateContent(content: EmptyStateContent.() -> Unit = {}): EmptyStateContent =
    register(EmptyStateContent(), content)

fun EmptyStateContent.pfEmptyStateBody(content: EmptyStateBody.() -> Unit = {}): EmptyStateBody =
    register(EmptyStateBody(), content)

fun EmptyStateContent.pfEmptyStateSecondary(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "empty-state".component("secondary")), content)

// ------------------------------------------------------ tag

class EmptyState(iconClass: String, title: String, size: Size?, content: EmptyStateContent.() -> Unit) :
    PatternFlyTag<HTMLDivElement>(ComponentType.EmptyState, "div", "empty-state".component()) {
    init {
        size?.let {
            domNode.classList.add(it.modifier)
        }
        pfEmptyStateContent {
            pfIcon(iconClass).apply {
                domNode.classList.add("empty-state".component("icon"))
            }
            pfTitle(title, size = Size.lg)
            content(this)
        }
    }
}

class EmptyStateBody : Tag<HTMLDivElement>("div", baseClass = "empty-state".component("body")), WithText<HTMLDivElement>

class EmptyStateContent : Tag<HTMLDivElement>("div", baseClass = "empty-state".component("content"))
