package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.renderElement
import kotlinx.coroutines.Job
import org.patternfly.ButtonVariation.link
import org.patternfly.Size.LG
import org.patternfly.Size.MD
import org.patternfly.Size.SM
import org.patternfly.Size.XL
import org.patternfly.Size.XL_4
import org.patternfly.Size.XS
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

/**
 * Creates an [EmptyState] component.
 *
 * @param size the size of the empty state component. Supported sizes are [Size.XL], [Size.LG], [Size.SM] and [Size.XS]
 * @param iconClass an optional icon class
 * @param title the title of the empty state component
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the [EmptyStateContent] component
 *
 * @sample org.patternfly.sample.EmptyStateSample.emptyState
 */
public fun RenderContext.emptyState(
    size: Size = MD,
    iconClass: String? = null,
    title: String,
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = register(EmptyState(size, iconClass, title, id = id, baseClass = baseClass, job, content), {})

/**
 * Creates an [EmptyState] component containing a [Spinner] and a "Loading" header.
 *
 * @param title the title of the empty state component
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the [EmptyStateContent] component
 */
public fun RenderContext.emptyStateSpinner(
    title: String = "Loading",
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = emptyState(title = title, id = id, baseClass = baseClass, content = content).apply {
    domNode.querySelector(ComponentType.Title)?.prepend(
        renderElement {
            div(baseClass = "empty-state".component("icon")) {
                spinner()
            }
        }.domNode
    )
}

/**
 * Creates an [EmptyState] that can be used when a filter does not return results.
 *
 * @param title the title of the empty state component
 * @param body the text shown in the empty state body
 * @param action text and handler for the primary action
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the [EmptyStateContent] component
 */
public fun RenderContext.emptyStateNoResults(
    title: String = "No results found",
    body: Div.() -> Unit = {
        +"No results match the filter criteria. Remove all filters or clear all filters to show results."
    },
    action: Pair<String, Handler<Unit>>? = null,
    id: String? = null,
    baseClass: String? = null,
    content: EmptyStateContent.() -> Unit = {}
): EmptyState = emptyState(iconClass = "search".fas(), title = title, id = id, baseClass = baseClass) {
    emptyStateBody {
        body(this)
    }
    action?.let { (text, handler) ->
        emptyStatePrimary {
            clickButton(link) {
                +text
            } handledBy handler
        }
    }
    content(this)
}

/**
 * Creates a [Div] container for the body of the [EmptyState] component. Use this function to add a description or other text related content.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun EmptyStateContent.emptyStateBody(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("body"), baseClass), job), content)

/**
 * Creates a [Div] container for the primary action of the [EmptyState] component. Use this function if you have a special use case like multiple elements as primary action.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.EmptyStateSample.primaryContainer
 */
public fun EmptyStateContent.emptyStatePrimary(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("primary"), baseClass), job), content)

/**
 * Creates a [Div] container for the secondary actions of the [EmptyState] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun EmptyStateContent.emptyStateSecondary(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("empty-state".component("secondary"), baseClass), job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [empty state](https://www.patternfly.org/v4/components/empty-state/design-guidelines) component.
 *
 * An empty state component fills a screen that is not yet populated with data or information. All elements are nested inside the [EmptyStateContent] component. This includes the icon, the header and the primary and secondary buttons.
 *
 * An empty state should contain an icon, must contain a title and should contain a body and buttons. The body must be nested inside an [emptyStateBody] container. The primary button can be added directly as a [PushButton] to the [EmptyStateContent] or nested inside a [emptyStatePrimary] container. Secondary button(s) must be added inside a [emptyStateSecondary] container.
 *
 * ```
 * ┏━━━━━━━━━━━ emptyState: EmptyState ━━━━━━━━━━┓
 * ┃                                             ┃
 * ┃ ┌──emptyStateContent: EmptyStateContent───┐ ┃
 * ┃ │ ┌─────────────────────────────────────┐ │ ┃
 * ┃ │ │         emptyStateBody: Div         │ │ ┃
 * ┃ │ └─────────────────────────────────────┘ │ ┃
 * ┃ │ ┌─────────────────────────────────────┐ │ ┃
 * ┃ │ │       emptyStatePrimary: Div        │ │ ┃
 * ┃ │ └─────────────────────────────────────┘ │ ┃
 * ┃ │ ┌─────────────────────────────────────┐ │ ┃
 * ┃ │ │      emptyStateSecondary: Div       │ │ ┃
 * ┃ │ └─────────────────────────────────────┘ │ ┃
 * ┃ └─────────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 *
 * @sample org.patternfly.sample.EmptyStateSample.emptyState
 */
public class EmptyState internal constructor(
    size: Size,
    iconClass: String?,
    title: String,
    id: String?,
    baseClass: String?,
    job: Job,
    content: EmptyStateContent.() -> Unit
) : PatternFlyComponent<HTMLDivElement>, Div(
    id = id,
    baseClass = classes {
        +ComponentType.EmptyState
        +(size.modifier `when` (size == XL || size == LG || size == SM || size == XS))
        +baseClass
    },
    job
) {

    init {
        markAs(ComponentType.EmptyState)
        register(EmptyStateContent(id = null, baseClass = null, job = job)) { esc ->
            with(esc) {
                if (iconClass != null) {
                    icon(iconClass, baseClass = "empty-state".component("icon"))
                }
                val titleSize = when (size) {
                    XL -> XL_4
                    XS -> MD
                    else -> LG
                }
                title(size = titleSize) { +title }
                content(esc)
            }
        }
    }
}

/**
 * Empty state content component.
 */
public class EmptyStateContent internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("empty-state".component("content"), baseClass), job)
