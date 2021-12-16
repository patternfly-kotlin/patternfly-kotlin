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
 * @param router if given, the specified router is used for navigation
 * @param noHomeLink set to `true`, if the first item should be plain text only
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.breadcrumb(
    router: Router<T>? = null,
    noHomeLink: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Breadcrumb<T>.() -> Unit = {}
) {
    Breadcrumb(router, noHomeLink).apply(context).render(this, baseClass, id)
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
 * @sample org.patternfly.sample.BreadcrumbSample.routerItems
 */
public open class Breadcrumb<T>(
    private val router: Router<T>?,
    private var noHomeLink: Boolean = false
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val staticItems: MutableList<BreadcrumbItem<T>> = mutableListOf()
    private var dynamicItems: Flow<List<T>>? = null
    private var idProvider: IdProvider<T, String> = { Id.build(it.toString()) }
    private var display: (BreadcrumbItem<T>.(T) -> Unit)? = null
    private var selectionStore: RootStore<T?> = storeOf(null)
    private var firstItem: Boolean = true

    /**
     * [Flow] containing the payload of the selected [BreadcrumbItem]s.
     */
    public val selections: Flow<T> = selectionStore.data.mapNotNull { it }

    /**
     * Whether to render the first item as a link or not.
     */
    public fun noHomeLink(noHomeLink: Boolean) {
        this.noHomeLink = noHomeLink
    }

    /**
     * Adds a [BreadcrumbItem].
     */
    public fun item(value: T, context: BreadcrumbItem<T>.() -> Unit = {}) {
        BreadcrumbItem(value).apply(context).also {
            staticItems.add(it)
        }
    }

    /**
     * Adds the items from the specified store.
     */
    public fun items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: (BreadcrumbItem<T>.(T) -> Unit)
    ) {
        items(values.data, idProvider, display)
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: (BreadcrumbItem<T>.(T) -> Unit)
    ) {
        this.dynamicItems = values
        this.idProvider = idProvider
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
                    staticItems.forEach { item ->
                        renderItem(this, item)
                    }
                    dynamicItems?.let { values ->
                        values.renderEach(into = this, idProvider = idProvider) { value ->
                            val item = BreadcrumbItem(value)
                            display?.invoke(item, value)
                            renderItem(this, item)
                        }
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: BreadcrumbItem<T>): RenderContext =
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

/**
 * An item in an [Breadcrumb] component. The item consists of a typed data. Use string as type, if you just want to build a normal breadcrumb:
 *
 * @sample org.patternfly.sample.BreadcrumbSample.staticItems
 */
public class BreadcrumbItem<T> internal constructor(public val data: T) :
    WithExpandedStore by ExpandedStoreMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    init {
        this.title(data.toString())
    }
}
