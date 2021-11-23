package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import dev.fritz2.routing.Router
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates a new [Breadcrumb] component.
 *
 * @param store the source for the [items][BreadcrumbItem]
 * @param idProvider identifier for the data in the store
 * @param router if given, the specified router is used for navigation
 * @param noHomeLink set to `true`, if the first item should be plain text only
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.breadcrumb(
    store: Store<List<T>>? = null,
    idProvider: IdProvider<T, String>? = null,
    router: Router<T>? = null,
    noHomeLink: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Breadcrumb<T>.() -> Unit = {}
) {
    Breadcrumb(
        store = store,
        idProvider = idProvider,
        router = router,
        noHomeLink = noHomeLink
    ).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [breadcrumb](https://www.patternfly.org/v4/components/breadcrumb/design-guidelines) component.
 *
 * A breadcrumb provides page context to help users navigate more efficiently and understand where they are in the application hierarchy.
 *
 * The [items][BreadcrumbItem] can be added statically or by using a store. See the samples for more details.
 *
 * @sample org.patternfly.sample.BreadcrumbSample.staticItems
 * @sample org.patternfly.sample.BreadcrumbSample.storeItems
 * @sample org.patternfly.sample.BreadcrumbSample.routerItems
 */
public class Breadcrumb<T>(
    private val store: Store<List<T>>?,
    private val idProvider: IdProvider<T, String>?,
    private val router: Router<T>?,
    private var noHomeLink: Boolean = false
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val entries: MutableList<BreadcrumbItem<T>> = mutableListOf()
    private var display: ((T) -> BreadcrumbItem<T>)? = null
    private var selectionStore: RootStore<T?> = storeOf(null)
    public val selections: Flow<T> = selectionStore.data.mapNotNull { it }

    public fun noHomeLink(noHomeLink: Boolean) {
        this.noHomeLink = noHomeLink
    }

    public fun item(data: T, context: BreadcrumbItem<T>.() -> Unit = {}): BreadcrumbItem<T> =
        BreadcrumbItem(data).apply(context).also {
            entries.add(it)
        }

    /**
     * Defines how to render [breadcrumb items][BreadcrumbItem] when using a store.
     */
    public fun display(display: (T) -> BreadcrumbItem<T>) {
        this.display = display
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            nav(baseClass = classes(ComponentType.Breadcrumb, baseClass), id = id) {
                markAs(ComponentType.Breadcrumb)
                aria["label"] = "breadcrumb"
                applyElement(this)
                applyEvents(this)

                ol(baseClass = "breadcrumb".component("list")) {
                    if (store != null) {
                        val idp = idProvider ?: { Id.build(it.toString()) }
                        store.data.map { it.withIndex().toList() }.renderEach(
                            idProvider = { idp.invoke(it.value) },
                            content = { (index, data) ->
                                val display = this@Breadcrumb.display ?: { item(data) }
                                renderItem(this, display(data), index)
                            }
                        )
                    } else {
                        entries.forEachIndexed { index, item ->
                            renderItem(this, item, index)
                        }
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: BreadcrumbItem<T>, index: Int): RenderContext =
        with(context) {
            li(baseClass = "breadcrumb".component("item")) {
                span(baseClass = "breadcrumb".component("item", "divider")) {
                    icon("angle-right".fas())
                }
                if (index == 0 && noHomeLink) {
                    item.applyEvents(this)
                    item.applyTitle(this)
                } else {
                    a(baseClass = "breadcrumb".component("link")) {
                        classMap(
                            selectionStore.data.map {
                                mapOf("current".modifier() to (item.data == it))
                            }
                        )
                        router?.let { router ->
                            aria["current"] = router.data.map { route ->
                                if (route == item.data) "page" else ""
                            }
                            clicks.map { item.data } handledBy router.navTo
                        }
                        clicks.map { item.data } handledBy selectionStore.update
                        item.applyEvents(this)
                        item.applyTitle(this)
                    }
                }
            }
        }
}

// ------------------------------------------------------ item

public class BreadcrumbItem<T> internal constructor(public val data: T) :
    WithExpandedStore by ExpandedStoreMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    init {
        this.title(data.toString())
    }
}
