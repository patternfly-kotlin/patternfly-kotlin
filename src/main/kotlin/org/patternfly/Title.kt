package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLHeadingElement

// ------------------------------------------------------ dsl

public fun RenderContext.title(
    level: Int = 1,
    size: Size = Size.XL_2,
    id: String? = null,
    baseClass: String? = null,
    content: Title.() -> Unit = {}
): Title = register(Title(level, size, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

public class Title internal constructor(
    level: Int = 1,
    size: Size = Size.XL_2,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLHeadingElement>,
    H(level, id = id, baseClass = classes {
        +ComponentType.Title
        +size.modifier
        +baseClass
    }, job) {

    init {
        markAs(ComponentType.Title)
    }
}
