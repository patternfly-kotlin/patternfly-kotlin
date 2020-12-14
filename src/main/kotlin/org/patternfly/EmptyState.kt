package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLDivElement

// TODO Document me
// ------------------------------------------------------ dsl

public fun RenderContext.emptyState(
    iconClass: String,
    title: String,
    size: Size? = null,
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(iconClass, title, size, id = id, baseClass = baseClass, job, content), {})

public fun EmptyState.emptyStateContent(
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyStateContent = register(EmptyStateContent(id = id, baseClass = baseClass, job), content)

public fun EmptyStateContent.emptyStateBody(
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateBody.() -> Unit = {}
): EmptyStateBody = register(EmptyStateBody(id = id, baseClass = baseClass, job), content)

public fun EmptyStateContent.emptyStateSecondary(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("secondary"), baseClass), job), content)

// ------------------------------------------------------ tag

public class EmptyState internal constructor(
    iconClass: String,
    title: String,
    size: Size?,
    id: String?,
    baseClass: String?,
    job: Job,
    content: EmptyStateContent.() -> Unit
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes {
    +ComponentType.EmptyState
    +size?.modifier
    +baseClass
}, job) {

    init {
        markAs(ComponentType.EmptyState)
        emptyStateContent {
            icon(iconClass).apply {
                domNode.classList.add("empty-state".component("icon"))
            }
            title(size = Size.LG) {
                +title
            }
            content(this)
        }
    }
}

public class EmptyStateBody internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("empty-state".component("body"), baseClass), job)

public class EmptyStateContent internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("empty-state".component("content"), baseClass), job)
