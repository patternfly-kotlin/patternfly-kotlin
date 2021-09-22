package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import org.patternfly.Level.H1
import org.w3c.dom.HTMLHeadingElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Title] component.
 *
 * @param level the level of the heading
 * @param size the size of the heading
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.title(
    level: Level = H1,
    size: Size = level.size,
    id: String? = null,
    baseClass: String? = null,
    content: Title.() -> Unit = {}
): Title = register(Title(level, size, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [title](https://www.patternfly.org/v4/components/title/design-guidelines/) component.
 *
 * A title component applies top and bottom margins, font-weight, font-size, and line-height to titles.
 *
 * @sample org.patternfly.sample.TitleSample.title
 */
public class Title internal constructor(
    level: Level,
    size: Size,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLHeadingElement>,
    H(
        level.level,
        id = id,
        baseClass = classes {
            +ComponentType.Title
            +size.modifier
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Title)
    }
}
