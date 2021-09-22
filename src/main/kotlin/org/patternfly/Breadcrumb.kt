package org.patternfly

import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TextElement
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.component.markAs
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Breadcrumb] component.
 *
 * @param store the store for the breadcrumb items
 * @param noHomeLink set to `true`, if the first item should be plain text only
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.breadcrumb(
    store: BreadcrumbStore<T> = BreadcrumbStore(),
    noHomeLink: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Breadcrumb<T>.() -> Unit = {}
): Breadcrumb<T> = register(Breadcrumb(store, noHomeLink, id = id, baseClass = baseClass, job), content)

/**
 * Starts a block to add navigation items using the DSL.
 *
 * @param block code block for adding the navigation items.
 */
public fun <T> Breadcrumb<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

/**
 * Starts a block to add navigation items using the DSL.
 *
 * @param block code block for adding the navigation items.
 */
public fun <T> BreadcrumbStore<T>.updateItems(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(idProvider, itemSelection).apply(block).build()
    update(entries)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [breadcrumb](https://www.patternfly.org/v4/components/breadcrumb/design-guidelines) component.
 *
 * A breadcrumb provides page context to help users navigate more efficiently and understand where they are in the application hierarchy.
 *
 * The data in the breadcrumb is managed by a [BreadcrumbStore] and is wrapped inside instances of [Item].
 *
 * ### Adding entries
 *
 * Entries can be added by using the [BreadcrumbStore] or by using the DSL.
 *
 * ### Rendering entries
 *
 * By default the breadcrumb uses a builtin function to render the [Item]s in the [BreadcrumbStore]. This function takes the [Item.text] into account (if specified). If [Item.text] is `null`, the builtin function falls back to `Item.item.toString()`.
 *
 * If you don't want to use the builtin defaults you can specify a custom display function by calling [display]. In this case you have full control over the rendering of the data in the options menu entries.
 *
 * @sample org.patternfly.sample.BreadcrumbSample.breadcrumb
 */
public class Breadcrumb<T> internal constructor(
    public val store: BreadcrumbStore<T>,
    noHomeLink: Boolean,
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
        job = job,
        scope = Scope()
    ) {

    private var customDisplay: ComponentDisplay<A, T>? = null
    private var defaultDisplay: ComponentDisplay<A, Item<T>> = { item ->
        +(item.text ?: item.item.toString())
    }

    init {
        markAs(ComponentType.Breadcrumb)
        aria["label"] = "breadcrumb"
        ol(baseClass = "breadcrumb".component("list")) {
            this@Breadcrumb.store.data.map { it.items.withIndex().toList() }.renderEach { (index, item) ->
                li(baseClass = "breadcrumb".component("item")) {
                    if (index == 0 && noHomeLink) {
                        a(baseClass = "breadcrumb".component("link")) {
                            inlineStyle(
                                """
                                color: var(--pf-c-breadcrumb__link--m-current--Color);
                                text-decoration: none;
                                cursor: default;
                                """.trimIndent()
                            )
                            if (this@Breadcrumb.customDisplay != null) {
                                this@Breadcrumb.customDisplay?.invoke(this, item.item)
                            } else {
                                this@Breadcrumb.defaultDisplay.invoke(this, item)
                            }
                        }
                    } else {
                        span(baseClass = "breadcrumb".component("item", "divider")) {
                            icon("angle-right".fas())
                        }
                        a(baseClass = "breadcrumb".component("link")) {
                            classMap(
                                this@Breadcrumb.store.singleSelection.map {
                                    mapOf("current".modifier() to item.selected)
                                }
                            )
                            clicks.map { item.unwrap() } handledBy this@Breadcrumb.store.handleClicks
                            if (this@Breadcrumb.customDisplay != null) {
                                this@Breadcrumb.customDisplay?.invoke(this, item.item)
                            } else {
                                this@Breadcrumb.defaultDisplay.invoke(this, item)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets a custom display function to render the data inside the breadcrumb.
     */
    public fun display(display: ComponentDisplay<A, T>) {
        this.customDisplay = display
    }
}

// ------------------------------------------------------ store

/**
 * An [EntriesStore] with [ItemSelection.SINGLE] selection mode.
 */
public class BreadcrumbStore<T>(idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.SINGLE)
