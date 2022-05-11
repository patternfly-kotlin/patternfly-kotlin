package org.patternfly

import dev.fritz2.dom.html.RenderContext

// ------------------------------------------------------ factory

/**
 * Creates a [TextContent] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param content a lambda expression for setting up the text content
 */
public fun RenderContext.textContent(
    baseClass: String? = null,
    id: String? = null,
    content: RenderContext.() -> Unit = {}
) {
    TextContent(content).render(this, baseClass, id)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [text content](https://www.patternfly.org/v4/components/text/design-guidelines) component.
 *
 * A text component can wrap any static HTML content you want to place on your page to provide correct formatting when using standard HTML tags.
 *
 * @sample org.patternfly.sample.TextContentSample.textContent
 */
public open class TextContent(private val content: RenderContext.() -> Unit) : PatternFlyComponent<Unit> {

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(classes(ComponentType.TextContent, baseClass)) {
                markAs(ComponentType.TextContent)
                content.invoke(this)
            }
        }
    }
}
