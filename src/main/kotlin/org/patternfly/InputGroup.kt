package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLDivElement

// TODO Document me
// ------------------------------------------------------ dsl

public fun RenderContext.inputGroup(
    id: String? = null,
    baseClass: String? = null,
    content: InputGroup.() -> Unit = {}
): InputGroup = register(InputGroup(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

public class InputGroup internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.InputGroup, baseClass), job, Scope()) {

    init {
        markAs(ComponentType.InputGroup)
    }
}
