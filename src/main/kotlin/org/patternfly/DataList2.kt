package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.Id
import org.w3c.dom.HTMLLIElement

// ------------------------------------------------------ factory

/**
 * Creates an [DataList2] component.
 *
 * @param compact whether to use compact layout
 * @param selectable whether the datalist items are selectable
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.dataList2(
    compact: Boolean = false,
    selectable: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: DataList2.() -> Unit = {}
) {
    DataList2(compact = compact, selectable = selectable).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

public open class DataList2(private val compact: Boolean, private val selectable: Boolean) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var storeItems: Boolean = false
    private val itemStore: DataListItemStore = DataListItemStore()
    private val headItems: MutableList<DataListItem2> = mutableListOf()
    private val tailItems: MutableList<DataListItem2> = mutableListOf()
    private val idSingleSelection: RootStore<String?> = storeOf(null)
    private var modelSingleSelection: RootStore<*>? = null
//    private val idMultiSelection: RootStore<List<String>> = storeOf(emptyList())

    public fun item(context: DataListItem2.() -> Unit) {
        (if (storeItems) tailItems else headItems).add(
            DataListItem2(Id.unique(ComponentType.DataList.id, "itm")).apply(context)
        )
    }

    public fun <T> items(
        values: Store<List<T>>,
        selection: RootStore<T?> = storeOf(null),
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: DataListItems.(T) -> DataListItem2
    ) {
        items(values.data, selection, idProvider, display)
    }

    public fun <T> items(
        values: Flow<List<T>>,
        selection: RootStore<T?> = storeOf(null),
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: DataListItems.(T) -> DataListItem2
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                val map = values.associateBy { idProvider(it) }
                itemStore.update(
                    values.map { value ->
                        DataListItems(idProvider(value)).run {
                            val item = display.invoke(this, value)
                            item.onSelect<T> {
                                selection.update(it)
                            }
                            item
                        }
                    }
                )
            }
        }
        storeItems = true
        modelSingleSelection = selection
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
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
        }
    }

    private fun renderItem(context: RenderContext, item: DataListItem2): Tag<HTMLLIElement> = with(context) {
        li(
            baseClass = classes {
                +"data-list".component("item")
                +("selectable".modifier() `when` selectable)
            }
        ) {
            aria["labelledby"] = item.id
            if (selectable) {
                attr("tabindex", 0)
                val idSelected = idSingleSelection.data.map { it == item.id }
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
                clicks.map { item.id } handledBy idSingleSelection.update
            } else {
                with(item.expandedStore) {
                    toggleExpanded()
                }
            }
            renderRow(this, item)
            renderContent(this, item)
        }
    }

    private fun renderRow(context: RenderContext, item: DataListItem2) {
        with(context) {
            div(baseClass = "data-list".component("item", "row")) {
                renderControls(this, item)
                renderCells(this, item)
                renderActions(this, item)
            }
        }
    }

    private fun renderControls(context: RenderContext, item: DataListItem2) {
        if (item.controls) {
            with(context) {
                div(baseClass = "data-list".component("item", "control")) {
                    if (selectable) {
                        domNode.onclick = { it.stopPropagation() }
                    }
                    if (item.toggle) {
                        div(baseClass = "data-list".component("toggle")) {
                            clickButton(variants = arrayOf(plain), id = item.toggleId) {
                                aria["controls"] = item.contentId
                                aria["label"] = "Details"
                                aria["labelledby"] = "${item.id} ${item.toggleId}"
                                aria["expanded"] = item.expandedStore.data.map { it.toString() }
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
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderCells(context: RenderContext, item: DataListItem2) {
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

    private fun renderActions(context: RenderContext, item: DataListItem2) {
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

    private fun renderContent(context: RenderContext, item: DataListItem2) {
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

public class DataListItems(internal val id: String) {

    public fun item(context: DataListItem2.() -> Unit): DataListItem2 = DataListItem2(id).apply(context)
}

public class DataListItem2(public val id: String) :
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

    internal fun <T> onSelect(block: (T) -> Unit) {
        TODO("onSelect() not yet implemented")
    }

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

internal class DataListItemStore : RootStore<List<DataListItem2>>(emptyList())
