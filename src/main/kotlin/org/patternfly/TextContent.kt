package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

/**
 * Creates a [TextContent] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.textContent(
    id: String? = null,
    baseClass: String? = null,
    content: TextContent.() -> Unit = {}
): TextContent = register(TextContent(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [text content](https://www.patternfly.org/v4/components/text/design-guidelines) component.
 *
 * A text component can wrap any static HTML content you want to place on your page to provide correct formatting when using standard HTML tags.
 *
 * @sample org.patternfly.sample.TextContentSample.textContent
 */
public class TextContent internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.TextContent, baseClass), job) {

    init {
        markAs(ComponentType.TextContent)
    }
}
