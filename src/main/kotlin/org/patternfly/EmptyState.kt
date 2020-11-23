package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfEmptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, id = id, baseClass = baseClass, content), {})

public fun EmptyState.pfEmptyStateContent(
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyStateContent = register(EmptyStateContent(id = id, baseClass = baseClass), content)

public fun EmptyStateContent.pfEmptyStateBody(
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateBody.() -> Unit = {}
): EmptyStateBody = register(EmptyStateBody(id = id, baseClass = baseClass), content)

public fun EmptyStateContent.pfEmptyStateSecondary(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("secondary"), baseClass)), content)

// ------------------------------------------------------ tag

public class EmptyState internal constructor(
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
            icon(iconClass).apply {
                domNode.classList.add("empty-state".component("icon"))
            }
            pfTitle(size = Size.LG) {
                +title
            }
            content(this)
        }
    }
}

public class EmptyStateBody internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("empty-state".component("body"), baseClass))

public class EmptyStateContent internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("empty-state".component("content"), baseClass))
