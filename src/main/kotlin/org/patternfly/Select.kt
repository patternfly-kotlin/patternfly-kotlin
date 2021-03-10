package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import org.patternfly.dom.Id
import org.w3c.dom.HTMLDivElement

// TODO Implement and document me
// ------------------------------------------------------ dsl

public fun RenderContext.select(
    id: String? = null,
    baseClass: String? = null,
    content: Select.() -> Unit = {}
): Select = register(Select(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

public class Select internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.Select, baseClass), job) {

    init {
        markAs(ComponentType.Select)
    }
}

// ------------------------------------------------------ store

public class SingleSelectStore<T>(override val idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.SINGLE)

public class MultiSelectStore<T>(override val idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.MULTIPLE)
