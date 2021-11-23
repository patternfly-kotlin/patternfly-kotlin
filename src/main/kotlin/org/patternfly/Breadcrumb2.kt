package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import dev.fritz2.routing.Router
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

// ------------------------------------------------------ factory

public fun <T> RenderContext.breadcrumb2(
    noHomeLink: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Breadcrumb2<T>.() -> Unit = {}
) {
    Breadcrumb2<T>(
        router = null,
        store = null,
        idProvider = null,
        noHomeLink = noHomeLink
    ).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

public class Breadcrumb2<T>(
    private val router: Router<T>?,
    private val store: Store<List<T>>?,
    private val idProvider: IdProvider<T, String>?,
    private var noHomeLink: Boolean = false
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val entries: MutableList<BreadcrumbItem<T>> = mutableListOf()
    private var display: ((T) -> BreadcrumbItem<T>)? = null
    private var selectionStore: BreadcrumbSelectionStore<T> = BreadcrumbSelectionStore()

    public val selections: Flow<T>
        get() = selectionStore.data.mapNotNull { it } // don't know why filterNotNull cannot be used here!?

    public fun noHomeLink(noHomeLink: Boolean) {
        this.noHomeLink = noHomeLink
    }

    public fun item(data: T, context: BreadcrumbItem<T>.() -> Unit = {}): BreadcrumbItem<T> =
        BreadcrumbItem(data).apply(context).also {
            entries.add(it)
        }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            nav(baseClass = classes(ComponentType.Breadcrumb, baseClass), id = id) {
                markAs(ComponentType.Breadcrumb)
                aria["label"] = "breadcrumb"
                applyElement(this)
                applyEvents(this)

                ol(baseClass = "breadcrumb".component("list")) {
                    if (store != null && idProvider != null) {
                        store.data.map { it.withIndex().toList() }.renderEach(
                            idProvider = { idProvider.invoke(it.value) },
                            content = { (index, data) ->
                                val display = this@Breadcrumb2.display ?: { item(data) }
                                renderItem(this, display(data), index)
                            }
                        )
                    } else {
                        entries.forEachIndexed() { index, item ->
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
                        }
                        clicks.map { item.data } handledBy selectionStore.select
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

// ------------------------------------------------------ store

internal class BreadcrumbSelectionStore<T> : RootStore<T?>(null) {
    val select: Handler<T> = handle { _, data -> data }
}
