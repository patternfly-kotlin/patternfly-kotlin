package org.patternfly

import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.lenses.IdProvider
import dev.fritz2.routing.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Breadcrumb] component.
 *
 * @param store the store for the breadcrumb items
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.breadcrumb(
    router: Router<T>,
    store: BreadcrumbStore<T> = BreadcrumbStore(),
    id: String? = null,
    baseClass: String? = null,
    content: Breadcrumb<T>.() -> Unit = {}
): Breadcrumb<T> = register(Breadcrumb(router, store, id = id, baseClass = baseClass, job), content)

/**
 * Starts a block to add navigation items using the DSL.
 *
 * @param block code block for adding the navigation items.
 */
public fun <T> Breadcrumb<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [breadcrumb](https://www.patternfly.org/v4/components/breadcrumb/design-guidelines) component.
 *
 * A breadcrumb provides page context to help users navigate more efficiently and understand where they are in the application hierarchy.
 *
 * @sample org.patternfly.sample.BreadcrumbSample.breadcrumb
 */
public class Breadcrumb<T> internal constructor(
    internal val router: Router<T>,
    internal val store: BreadcrumbStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLElement>,
    TextElement(
        "nav",
        id = id,
        baseClass = classes {
            +ComponentType.Breadcrumb
            +baseClass
        },
        job
    ) {

    private var display: ComponentDisplay<A, T>? = null

    init {
        markAs(ComponentType.Breadcrumb)
        aria["label"] = "breadcrumb"
        ol(baseClass = "breadcrumb".component("list")) {
            this@Breadcrumb.store.data.map { it.items }.renderEach { item ->
                li(baseClass = "breadcrumb".component("item")) {
                    span(baseClass = "breadcrumb".component("item", "divider")) {
                        icon("angle-right".fas())
                    }
                    a(baseClass = "breadcrumb".component("link")) {
                        classMap(
                            this@Breadcrumb.router.data.map { route ->
                                mapOf("current".modifier() to (route == item.unwrap()))
                            }
                        )
                        aria["current"] = this@Breadcrumb.router.data.map { route ->
                            if (route == item.unwrap()) "page" else ""
                        }
                        clicks.map { item.unwrap() } handledBy this@Breadcrumb.router.navTo
                        if (this@Breadcrumb.display != null) {
                            this@Breadcrumb.display?.invoke(this, item.unwrap())
                        } else {
                            +(item.text ?: "n/a")
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets a custom display function to render the navigation items.
     */
    public fun display(display: ComponentDisplay<A, T>) {
        this.display = display
    }
}

// ------------------------------------------------------ store

/**
 * An [EntriesStore] with [ItemSelection.SINGLE] selection mode.
 */
public class BreadcrumbStore<T>(idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.SINGLE)
