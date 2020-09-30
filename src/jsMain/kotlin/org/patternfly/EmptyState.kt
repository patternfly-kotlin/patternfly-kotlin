package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfEmptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, id = id, baseClass = baseClass, content), {})

fun EmptyState.pfEmptyStateContent(
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyStateContent = register(EmptyStateContent(id = id, baseClass = baseClass), content)

fun EmptyStateContent.pfEmptyStateBody(
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateBody.() -> Unit = {}
): EmptyStateBody = register(EmptyStateBody(id = id, baseClass = baseClass), content)

fun EmptyStateContent.pfEmptyStateSecondary(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("secondary"), baseClass)), content)

// ------------------------------------------------------ tag

class EmptyState(
    iconClass: String,
    title: String,
    size: Size?,
    id: String?,
    baseClass: String?,
    content: EmptyStateContent.() -> Unit
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.EmptyState
    +size?.modifier
    +baseClass
}) {
    init {
        markAs(ComponentType.EmptyState)
        pfEmptyStateContent {
            pfIcon(iconClass).apply {
                domNode.classList.add("empty-state".component("icon"))
            }
            pfTitle(size = Size.LG) {
                +title
            }
            content(this)
        }
    }
}

class EmptyStateBody(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("empty-state".component("body"), baseClass))

class EmptyStateContent(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("empty-state".component("content"), baseClass))
