package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.states
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.Id
import org.w3c.dom.HTMLLIElement

// ------------------------------------------------------ factory

/**
 * Creates an [DataList] component.
 *
 * @param selectionMode the selection mode for items
 * @param compact whether to use compact layout
 * @param selectable whether the datalist items are selectable
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.dataList(
    selectionMode: SelectionMode = SelectionMode.NONE,
    compact: Boolean = false,
    selectable: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: DataList.() -> Unit = {}
): DataList =
    DataList(selectionMode, compact = compact, selectable = selectable).apply(context).render(this, baseClass, id)

// ------------------------------------------------------ component

/**
 * PatternFly [data list](https://www.patternfly.org/v4/components/data-list/design-guidelines) component.
 *
 * A data list is used to display large data sets when you need a flexible layout or need to include interactive content like charts.
 *
 * @sample org.patternfly.sample.DataListSample.dataList
 */
@Suppress("TooManyFunctions")
public open class DataList(
    private val selectionMode: SelectionMode,
    private val compact: Boolean,
    private val selectable: Boolean
) :
    PatternFlyComponent<DataList>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val singleIdSelection: SingleIdStore = SingleIdStore()
    private val multiIdSelection: MultiIdStore = MultiIdStore()
    private val itemStore: HeadTailItemStore<DataListItem> = HeadTailItemStore()

    public val selectedId: Flow<String?>
        get() = singleIdSelection.data

    public val selectedIds: Flow<List<String>>
        get() = multiIdSelection.data

    public fun item(
        id: String = Id.unique(ComponentType.DataList.id, "itm"),
        context: StaticDataListItem.() -> Unit
    ) {
        val item = StaticDataListItem(id).apply(context)
        itemStore.add(item)
        item.select(singleIdSelection, multiIdSelection)
    }

    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        singleSelection: Store<T?>? = null,
        multiSelection: Store<List<T>>? = null,
        display: DataListItemScope.(T) -> DataListItem
    ) {
        storeItems(values.data, idProvider, singleSelection, multiSelection, display)
    }

    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        singleSelection: Store<T?>? = null,
        multiSelection: Store<List<T>>? = null,
        display: DataListItemScope.(T) -> DataListItem
    ) {
        storeItems(values, idProvider, singleSelection, multiSelection, display)
    }

    private fun <T> storeItems(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String>,
        singleDataSelection: Store<T?>?,
        multiDataSelection: Store<List<T>>?,
        display: DataListItemScope.(T) -> DataListItem
    ) {
        itemStore.collect(values) { valueList ->
            val idToData = valueList.associateBy { idProvider(it) }
            itemStore.update(valueList) { value ->
                DataListItemScope(idProvider(value)).run {
                    display(this, value)
                }
            }

            // setup data bindings
            if (selectionMode == SelectionMode.SINGLE) {
                singleDataSelection?.let { sds ->
                    singleIdSelection.dataBinding(idToData, idProvider, sds)
                }
            } else if (selectionMode == SelectionMode.MULTI) {
                multiDataSelection?.let { mds ->
                    multiIdSelection.dataBinding(idToData, idProvider, mds)
                }
            }
        }
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

            itemStore.allItems.renderEach(into = this, idProvider = { it.id }) { item ->
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

/**
 * Visual modifiers for a [DataList].
 *
 * @see <a href="https://www.patternfly.org/v4/components/data-list/design-guidelines">https://www.patternfly.org/v4/components/data-list/design-guidelines</a>
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class DataListVariant(internal val modifier: String) {
    compact("compact".modifier()),
    selectable("selectable-raised".modifier()),
}

// ------------------------------------------------------ item & store

public class DataListItemScope internal constructor(internal val id: String) {

    public fun item(context: DataListItem.() -> Unit): DataListItem = DataListItem(id).apply(context)
}

public open class DataListItem internal constructor(public val id: String) :
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

public class StaticDataListItem internal constructor(id: String) : DataListItem(id) {

    private val selection: FlagOrFlow = FlagOrFlow(id)

    public fun selected(value: Boolean) {
        selection.flag = value
    }

    public fun selected(value: Flow<Boolean>) {
        selection.flow = value
    }

    internal fun select(singleIdSelection: SingleIdStore, multiIdSelection: MultiIdStore) {
        selection.singleSelect(singleIdSelection)
        selection.multiSelect(multiIdSelection)
    }
}
