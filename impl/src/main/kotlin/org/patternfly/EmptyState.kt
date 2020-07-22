package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements

// ------------------------------------------------------ dsl

fun HtmlElements.pfEmptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, content), {})

fun EmptyState.pfEmptyStateContent(content: EmptyStateContent.() -> Unit = {}): EmptyStateContent =
    register(EmptyStateContent(), content)

fun EmptyStateContent.pfEmptyStateBody(content: EmptyStateBody.() -> Unit = {}): EmptyStateBody =
    register(EmptyStateBody(), content)

fun EmptyStateContent.pfEmptyStateSecondary(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "empty-state".component("secondary")), content)

// ------------------------------------------------------ tag

class EmptyState(iconClass: String, title: String, size: Size?, content: EmptyStateContent.() -> Unit) :
    Div(baseClass = "empty-state".component()) {
    init {
        domNode.componentType(ComponentType.EmptyState)
        size?.let {
            domNode.classList += it.modifier
        }
        pfEmptyStateContent {
            pfIcon(iconClass).apply {
                domNode.classList.add("empty-state".component("icon"))
            }
            pfTitle(title, size = Size.LG)
            content(this)
        }
    }
}

class EmptyStateBody : Div(baseClass = "empty-state".component("body"))

class EmptyStateContent : Div(baseClass = "empty-state".component("content"))
