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
    classes: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, id = id, classes = classes, content), {})

fun EmptyState.pfEmptyStateContent(
    id: String? = null,
    classes: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyStateContent = register(EmptyStateContent(id = id, classes = classes), content)

fun EmptyStateContent.pfEmptyStateBody(
    id: String? = null,
    classes: String? = null,
    content: EmptyStateBody.() -> Unit = {}
): EmptyStateBody = register(EmptyStateBody(id = id, classes = classes), content)

fun EmptyStateContent.pfEmptyStateSecondary(
    id: String? = null,
    classes: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("secondary"), classes)), content)

// ------------------------------------------------------ tag

class EmptyState(
    iconClass: String,
    title: String,
    size: Size?,
    id: String?,
    classes: String?,
    content: EmptyStateContent.() -> Unit
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.EmptyState
    +size?.modifier
    +classes
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

class EmptyStateBody(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("empty-state".component("body"), classes))

class EmptyStateContent(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("empty-state".component("content"), classes))
