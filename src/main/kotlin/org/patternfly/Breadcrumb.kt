package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates a new [Breadcrumb] component.
 *
 * @param noHomeLink set to `true`, if the first item should be plain text only
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.breadcrumb(
    noHomeLink: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Breadcrumb.() -> Unit = {}
) {
    Breadcrumb(noHomeLink).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [breadcrumb](https://www.patternfly.org/v4/components/breadcrumb/design-guidelines) component.
 *
 * A breadcrumb provides page context to help users navigate more efficiently and understand where they are in the application hierarchy.
 *
 * The [items][BreadcrumbItem] can be added statically and/or by using a store. See the samples for more details.
 *
 * @sample org.patternfly.sample.BreadcrumbSample.staticItems
 * @sample org.patternfly.sample.BreadcrumbSample.dynamicItems
 */
public open class Breadcrumb(private var noHomeLink: Boolean = false) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var firstItem: Boolean = true
    private var storeItems: Boolean = false
    private val itemStore: BreadcrumbItemStore = BreadcrumbItemStore()
    private val headItems: MutableList<BreadcrumbItem> = mutableListOf()
    private val tailItems: MutableList<BreadcrumbItem> = mutableListOf()
    private val selectionStore: RootStore<String?> = storeOf(null)

    /**
     * Whether to render the first item as a link or not.
     */
    public fun noHomeLink(noHomeLink: Boolean) {
        this.noHomeLink = noHomeLink
    }

    /**
     * Adds a [BreadcrumbItem].
     */
    public fun item(title: String? = null, context: BreadcrumbItem.() -> Unit = {}) {
        (if (storeItems) tailItems else headItems).add(
            BreadcrumbItem(
                Id.unique(ComponentType.Breadcrumb.id, "itm"),
                title
            ).apply(context)
        )
    }

    /**
     * Adds the items from the specified store.
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: BreadcrumbItems.(T) -> BreadcrumbItem
    ) {
        items(values.data, idProvider, display)
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: BreadcrumbItems.(T) -> BreadcrumbItem
    ) {
        (MainScope() + NotificationStore.job).launch {
            values.collect { values ->
                itemStore.update(
                    values.map { value ->
                        BreadcrumbItems(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )
            }
        }
        storeItems = true
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            nav(baseClass = classes(ComponentType.Breadcrumb, baseClass), id = id) {
                markAs(ComponentType.Breadcrumb)
                aria["label"] = "breadcrumb"
                applyElement(this)
                applyEvents(this)

                ol(baseClass = "breadcrumb".component("list")) {
                    itemStore.data.map { items ->
                        headItems + items + tailItems
                    }.renderEach(into = this, idProvider = { it.id }) { item ->
                        renderItem(this, item)
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: BreadcrumbItem): RenderContext =
        with(context) {
            li(baseClass = "breadcrumb".component("item")) {
                span(baseClass = "breadcrumb".component("item", "divider")) {
                    icon("angle-right".fas())
                }
                if (firstItem && noHomeLink) {
                    item.applyEvents(this)
                    item.applyTitle(this)
                    firstItem = false
                } else {
                    a(baseClass = "breadcrumb".component("link")) {
                        classMap(
                            selectionStore.data.map {
                                mapOf("current".modifier() to (item.id == it))
                            }
                        )
                        clicks.map { item.id } handledBy selectionStore.update
                        item.applyEvents(this)

                        if (item.content != null) {
                            item.content?.let { it.context(this) }
                        } else {
                            item.applyTitle(this)
                        }
                    }
                }
            }
        }
}

// ------------------------------------------------------ item & store

/**
 * DSL scope class to create [BreadcrumbItem]s when using [Breadcrumb.items] functions.
 *
 * @sample org.patternfly.sample.BreadcrumbSample.dynamicItems
 */
public class BreadcrumbItems(internal val id: String) {

    /**
     * Creates and returns a new [BreadcrumbItem].
     */
    public fun item(title: String?, context: BreadcrumbItem.() -> Unit = {}): BreadcrumbItem =
        BreadcrumbItem(id, title).apply(context)
}

/**
 * An item in an [Breadcrumb] component. The item consists of a typed data. Use string as type, if you just want to build a normal breadcrumb:
 *
 * @sample org.patternfly.sample.BreadcrumbSample.staticItems
 */
public class BreadcrumbItem internal constructor(internal val id: String, title: String?) :
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var content: SubComponent<A>? = null

    init {
        title?.let { this.title(it) }
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: A.() -> Unit
    ) {
        this.content = SubComponent(baseClass, id, context)
    }
}

internal class BreadcrumbItemStore : RootStore<List<BreadcrumbItem>>(emptyList())
