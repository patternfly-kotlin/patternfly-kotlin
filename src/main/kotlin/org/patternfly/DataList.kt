package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.handledBy
import dev.fritz2.dom.states
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.Id
import org.w3c.dom.HTMLLIElement

// ------------------------------------------------------ factory

/**
 * Creates an [DataList] component.
 *
 * @param compact whether to use compact layout
 * @param selectable whether the datalist items are selectable
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.dataList(
    compact: Boolean = false,
    selectable: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: DataList.() -> Unit = {}
): DataList = DataList(compact = compact, selectable = selectable).apply(context).render(this, baseClass, id)

// ------------------------------------------------------ component

/**
 * PatternFly [data list](https://www.patternfly.org/v4/components/data-list/design-guidelines) component.
 *
 * A data list is used to display large data sets when you need a flexible layout or need to include interactive content like charts.
 *
 * @sample org.patternfly.sample.DataListSample.dataList
 */
@Suppress("TooManyFunctions")
public open class DataList(private val compact: Boolean, private val selectable: Boolean) :
    PatternFlyComponent<DataList>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var itemsInStore: Boolean = false
    private val itemStore: DataListItemStore = DataListItemStore()
    private val headItems: MutableList<DataListItem> = mutableListOf()
    private val tailItems: MutableList<DataListItem> = mutableListOf()
    private val singleIdSelection: RootStore<String?> = storeOf(null)
    private val multiIdSelection: MultiIdSelectionStore = MultiIdSelectionStore()

    public val selectedId: Flow<String?>
        get() = singleIdSelection.data

    public val selectedIds: Flow<List<String>>
        get() = multiIdSelection.data

    public fun item(
        id: String = Id.unique(ComponentType.DataList.id, "itm"),
        context: DataListItem.() -> Unit
    ) {
        (if (itemsInStore) tailItems else headItems).add(DataListItem(id).apply(context))
    }

    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        selection: Store<T?> = storeOf(null),
        display: DataListItemScope.(T) -> DataListItem
    ) {
        storeItems(values.data, idProvider, selection, null, display)
    }

    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        selection: Store<T?> = storeOf(null),
        display: DataListItemScope.(T) -> DataListItem
    ) {
        storeItems(values, idProvider, selection, null, display)
    }

    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        selection: Store<List<T>> = storeOf(emptyList()),
        display: DataListItemScope.(T) -> DataListItem
    ) {
        storeItems(values.data, idProvider, null, selection, display)
    }

    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        selection: Store<List<T>> = storeOf(emptyList()),
        display: DataListItemScope.(T) -> DataListItem
    ) {
        storeItems(values, idProvider, null, selection, display)
    }

    private fun <T> storeItems(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String>,
        singleDataSelection: Store<T?>?,
        multiDataSelection: Store<List<T>>?,
        display: DataListItemScope.(T) -> DataListItem
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                val idToData = values.associateBy { idProvider(it) }
                itemStore.update(
                    values.map { value ->
                        DataListItemScope(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )
                // setup two-way data bindings
                singleDataSelection?.let { sds ->
                    // id -> data
                    singleIdSelection.data.map { idToData[it] } handledBy sds.update
                    // data -> id
                    sds.data.map { if (it != null) idProvider(it) else null } handledBy singleIdSelection.update
                }
                multiDataSelection?.let { mds ->
                    // id -> data
                    multiIdSelection.data.map { ids ->
                        idToData.filterKeys { it in ids }
                    }.map { it.values.toList() } handledBy mds.update
                    // data -> id
                    mds.data.map { data -> data.map { idProvider(it) } } handledBy multiIdSelection.update
                }
            }
        }
        itemsInStore = true
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): DataList = with(context) {
        ul(
            baseClass = classes {
                +ComponentType.DataList
                +("compact".modifier() `when` compact)
                +baseClass
            },
            id = id
        ) {
            markAs(ComponentType.DataList)
            applyElement(this)
            applyEvents(this)
            attr("role", "list")

            itemStore.data.map { items ->
                headItems + items + tailItems
            }.renderEach(into = this, idProvider = { it.id }) { item ->
                renderItem(this, item)
            }
        }
        this@DataList
    }

    private fun renderItem(context: RenderContext, item: DataListItem): Tag<HTMLLIElement> = with(context) {
        li(
            baseClass = classes {
                +"data-list".component("item")
                +("selectable".modifier() `when` selectable)
            }
        ) {
            aria["labelledby"] = item.id
            if (selectable) {
                attr("tabindex", 0)
                val idSelected = singleIdSelection.data.map { it == item.id }
                classMap(
                    item.expandedStore.data.combine(idSelected) { expanded, selected ->
                        expanded to selected
                    }.map { (expanded, selected) ->
                        mapOf(
                            "expanded".modifier() to expanded,
                            "selected".modifier() to selected
                        )
                    }
                )
                clicks.map { item.id } handledBy singleIdSelection.update
            } else {
                with(item.expandedStore) {
                    toggleExpanded()
                }
            }
            renderRow(this, item)
            renderContent(this, item)
        }
    }

    private fun renderRow(context: RenderContext, item: DataListItem) {
        with(context) {
            div(baseClass = "data-list".component("item", "row")) {
                renderControls(this, item)
                renderCells(this, item)
                renderActions(this, item)
            }
        }
    }

    private fun renderControls(context: RenderContext, item: DataListItem) {
        if (item.controls) {
            with(context) {
                div(baseClass = "data-list".component("item", "control")) {
                    if (selectable) {
                        domNode.onclick = { it.stopPropagation() }
                    }
                    if (item.toggle) {
                        div(baseClass = "data-list".component("toggle")) {
                            clickButton(plain, id = item.toggleId) {
                                element {
                                    aria["controls"] = item.contentId
                                    aria["label"] = "Details"
                                    aria["labelledby"] = "${item.id} ${item.toggleId}"
                                    aria["expanded"] = item.expandedStore.data.map { it.toString() }
                                }
                                content {
                                    div(baseClass = "data-list".component("toggle", "icon")) {
                                        icon("angle-right".fas())
                                    }
                                }
                            } handledBy item.expandedStore.toggle
                        }
                    }
                    if (item.check) {
                        div(baseClass = "data-list".component("check")) {
                            input {
                                aria["labelledby"] = item.id
                                aria["invalid"] = false
                                attr("rowId", item.id)
                                name(item.checkId)
                                type("checkbox")
                                checked(multiIdSelection.data.map { it.contains(item.id) })
                                changes.states().map { checked -> item.id to checked } handledBy multiIdSelection.select
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderCells(context: RenderContext, item: DataListItem) {
        with(context) {
            div(baseClass = "data-list".component("item", "content")) {
                item.cells.forEach { cell ->
                    div(
                        baseClass = classes("data-list".component("cell"), cell.baseClass),
                        id = cell.id
                    ) {
                        cell.context(this)
                    }
                }
            }
        }
    }

    private fun renderActions(context: RenderContext, item: DataListItem) {
        with(context) {
            item.actions.forEach { action ->
                div(
                    baseClass = classes("data-list".component("item", "action"), action.baseClass),
                    id = action.id
                ) {
                    if (selectable) {
                        domNode.onclick = { it.stopPropagation() }
                    }
                    attr("rowId", item.id)
                    action.context(this)
                }
            }
        }
    }

    private fun renderContent(context: RenderContext, item: DataListItem) {
        item.content?.let { expandableContent ->
            with(context) {
                section(
                    baseClass = "data-list".component("expandable", "content"),
                    id = item.contentId
                ) {
                    with(item.expandedStore) {
                        hideIfCollapsed()
                    }
                    div(
                        baseClass = classes(
                            "data-list".component("expandable", "content", "body"),
                            expandableContent.baseClass
                        ),
                        id = expandableContent.id
                    ) {
                        expandableContent.context(this)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------ item & store

public class DataListItemScope(internal val id: String) {

    public fun item(context: DataListItem.() -> Unit): DataListItem = DataListItem(id).apply(context)
}

public class DataListItem(public val id: String) :
    WithExpandedStore by ExpandedStoreMixin() {

    internal var toggle: Boolean = false
    internal val toggleId: String = Id.build(id, "tgl")
    internal var check: Boolean = false
    internal val checkId: String = Id.build(id, "chk")
    internal val controls: Boolean
        get() = toggle || check
    internal val cells: MutableList<SubComponent<Div>> = mutableListOf()
    internal val actions: MutableList<SubComponent<Div>> = mutableListOf()
    internal var content: SubComponent<Div>? = null
    internal val contentId: String = Id.build(id, "cnt")

    public fun toggle() {
        toggle = true
    }

    public fun check() {
        check = true
    }

    public fun cellIcon(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit = {}
    ) {
        cell(baseClass = classes(baseClass, "icon".modifier()), id = id, context = context)
    }

    public fun cell(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit = {}
    ) {
        cells.add(SubComponent(baseClass, id, context))
    }

    public fun actionWrapper(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit = {}
    ) {
        actions.add(
            SubComponent(baseClass, id) {
                div(baseClass = "data-list".component("action")) {
                    context(this)
                }
            }
        )
    }

    public fun action(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit = {}
    ) {
        actions.add(SubComponent(baseClass, id, context))
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit = {}
    ) {
        content = SubComponent(baseClass, id, context)
    }
}

internal class DataListItemStore : RootStore<List<DataListItem>>(emptyList())

internal class MultiIdSelectionStore : RootStore<List<String>>(emptyList()) {

    val select: Handler<Pair<String, Boolean>> = handle { ids, (id, select) ->
        if (select) ids + id else ids - id
    }
}
