package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfContent(
    id: String? = null,
    baseClass: String? = null,
    content: Content.() -> Unit = {}
): Content = register(Content(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

public class Content internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Content, baseClass), job) {
    init {
        markAs(ComponentType.Content)
    }
}
