package org.patternfly

import dev.fritz2.dom.html.Caption
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TBody
import dev.fritz2.dom.html.Table
import dev.fritz2.dom.html.Td
import dev.fritz2.dom.html.Th
import dev.fritz2.dom.html.Tr
import dev.fritz2.dom.states
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.debug
import dev.fritz2.elemento.plusAssign
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.patternfly.DataTableSelection.MULTIPLE
import org.patternfly.DataTableSelection.MULTIPLE_ALL
import org.patternfly.DataTableSelection.SINGLE
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.set

// TODO <th> tooltips and popovers
//  custom row wrapper
//  compound expandable
//  width and breakpoint visibility modifiers
//  empty state
//  editable rows
// ------------------------------------------------------ dsl

/**
 * Creates a [DataTable] component.
 *
 * @param store the item store
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.dataTable(
    store: ItemStore<T> = ItemStore(),
    id: String? = null,
    baseClass: String? = null,
    content: DataTable<T>.() -> Unit = {}
): DataTable<T> = register(DataTable(store, id = id, baseClass = baseClass, job), content)

/**
 * Creates a caption above the data table.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun <T> DataTable<T>.dataTableCaption(
    id: String? = null,
    baseClass: String? = null,
    content: DataTableCaption.() -> Unit = {}
): DataTableCaption = register(DataTableCaption(id = id, baseClass = baseClass, job), content)

/**
 * Creates the builder for the columns. Please note that this does *not* create a visual container, but a builder for the DSL to add the columns.
 */
public fun <T> DataTable<T>.dataTableColumns(block: Columns<T>.() -> Unit) {
    columns.apply(block)
    renderTable()
}

/**
 * Creates a [DataColumn] to render the actual data from the [ItemStore]. Use this function multiple times to add columns for each item property you want to render.
 *
 * @param id the ID of the cell element
 * @param baseClass optional CSS class that should be applied to the cell element
 * @param block a lambda expression for setting up the [DataColumn]
 */
public fun <T> Columns<T>.dataTableColumn(
    text: String,
    id: String? = null,
    baseClass: String? = null,
    block: DataColumn<T>.() -> Unit
) {
    add(DataColumn(this, id, baseClass, text).apply(block))
}

/**
 * Creates a [SelectColumn] to select rows in the data table. Use this function once to add a checkbox or radio button depending on the value of [selectionMode].
 *
 * @param selectionMode controls how rows can be selected
 * @param id the ID of the cell element
 * @param baseClass optional CSS class that should be applied to the cell element
 */
public fun <T> Columns<T>.dataTableSelectColumn(
    selectionMode: DataTableSelection = MULTIPLE_ALL,
    id: String? = null,
    baseClass: String? = null
) {
    add(SelectColumn(this, id, baseClass, selectionMode))
}

/**
 * Creates a [ToggleColumn] to add an expandable row below each normal row. Use this function once to add a toggle icon in each row and to specify how the expandable content should look like.
 *
 * The width of the expandable content depends on the [fullWidth] parameter:
 * - `true`: the width uses all columns including toggle, select and all action columns
 * - `false` the width uses only the data columns
 *
 * @param fullWidth whether the expandable content uses the full width of the data table
 * @param noPadding whether to remove extra padding from the expandable content.
 * @param id the ID of the cell element
 * @param baseClass optional CSS class that should be applied to the cell element
 */
public fun <T> Columns<T>.dataTableToggleColumn(
    fullWidth: Boolean = false,
    noPadding: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    display: ComponentDisplay<Div, T>
) {
    add(ToggleColumn(this, id, baseClass, fullWidth, noPadding, display))
}

/**
 * Creates an [ActionColumn] to show buttons, dropdowns or other buttons or other action-like elements. Use this function multiple times for each action you want to add.
 *
 * @param id the ID of the cell element
 * @param baseClass optional CSS class that should be applied to the cell element
 * @param display defines how to render the action column
 */
public fun <T> Columns<T>.dataTableActionColumn(
    id: String? = null,
    baseClass: String? = null,
    display: ComponentDisplay<Td, T>
) {
    add(ActionColumn(this, id, baseClass, display))
}

// ------------------------------------------------------ tag

private val RADIO_GROUP_NAME = Id.unique(ComponentType.DataTable.id, "radio")

/**
 * PatternFly [table](https://www.patternfly.org/v4/components/table/design-guidelines) component.
 *
 * A table is used to display large data sets that can be easily laid out in a simple grid with column headers. The table contains different [Column]s to render the items of an [ItemStore]. Each item is rendered as one row.
 *
 * The columns are used to render both the header and the rows of the data table. The order in which you define the columns defines the visual representation of the data table. You can choose between these columns:
 *
 * 1. [ToggleColumn]: Use this column *exactly once* to add an expandable row below each normal row. The column uses a display function to render the expandable content. If used this column should be the first column.
 * 1. [SelectColumn]: Use this column *exactly once* to add a checkbox or a radio button which you can use to select rows. If used this column should be should be placed after the toggle column and before the data columns.
 * 1. [DataColumn]: Use this column *any number of times* to show the actual data of the items. The column uses display functions for the header and cells. One of the data columns should use the [ItemStore.idProvider] to assign an element ID. This ID is referenced by various [ARIA labelledby](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute) attributes.
 * 1. [ActionColumn]: Use this column *any number of times* to add buttons, dropdowns or other action-like elements. The column uses a display function for the cells. This column should be placed after the data columns.
 *
 * @sample DataTableSamples.dataTable
 */
public class DataTable<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLTableElement>, Table(id = id, baseClass = classes {
    +ComponentType.DataTable
    +"grid-md".modifier()
    +baseClass
}, job) {

    internal val columns: Columns<T> = Columns(this)

    init {
        attr("role", "grid")
        markAs(ComponentType.DataTable)
    }

    internal fun renderTable() {
        if (columns.hasToggle) {
            domNode.classList += "expandable".modifier()
        }

        thead {
            tr {
                attr("role", "row")
                this@DataTable.columns.forEach { column ->
                    when (column) {
                        is SelectColumn<T> -> {
                            if (column.selectionMode == MULTIPLE_ALL) {
                                td(baseClass = "table".component("check")) {
                                    attr("role", "cell")
                                    input {
                                        type("checkbox")
                                        aria["label"] = "Select all"
                                        changes.states().filter { !it }
                                            .map { } handledBy this@DataTable.itemStore.selectNone
                                        changes.states().filter { it }
                                            .map { } handledBy this@DataTable.itemStore.selectAll
                                    }
                                }
                            } else {
                                td {}
                            }
                        }
                        is DataColumn<T> -> {
                            th(baseClass = classes {
                                +("table".component("sort") `when` (column.sortInfo != null))
                            }) {
                                attr("scope", "col")
                                attr("role", "columnheader")
                                if (column.sortInfo != null) {
                                    aria["sort"] = this@DataTable.itemStore.data.map {
                                        if (it.sortInfo != null && it.sortInfo.id == column.sortInfo?.id) {
                                            if (it.sortInfo.ascending) "ascending" else "descending"
                                        } else "none"
                                    }
                                    classMap(this@DataTable.itemStore.data.map {
                                        mapOf("selected".modifier() to (column.sortInfo!!.id == it.sortInfo?.id))
                                    })
                                }
                                if (column.headerDisplay != null) {
                                    column.headerDisplay!!.invoke(this)
                                } else {
                                    if (column.sortInfo != null) {
                                        button(baseClass = "table".component("button")) {
                                            clicks.map { column.sortInfo!! } handledBy this@DataTable.itemStore.sortOrToggle
                                            span(baseClass = "table".component("text")) {
                                                +column.text
                                            }
                                            span(baseClass = "table".component("sort", "indicator")) {
                                                icon("arrows-alt-v".fas()) {
                                                    iconClass(this@DataTable.itemStore.data.map {
                                                        if (it.sortInfo != null && it.sortInfo.id == column.sortInfo?.id) {
                                                            if (it.sortInfo.ascending)
                                                                "long-arrow-alt-up".fas()
                                                            else
                                                                "long-arrow-alt-down".fas()
                                                        } else "arrows-alt-v".fas()
                                                    })
                                                }
                                            }
                                        }
                                    } else {
                                        +column.text
                                    }
                                }
                            }
                        }
                        is ToggleColumn<T>, is ActionColumn<T> -> td {}
                    }
                }
            }
        }

        if (columns.hasToggle) {
            // TODO implement shift(1) to keep the thead at index 0
            itemStore.visible.renderEach({ itemStore.idProvider(it) }, { item ->
                DataTableExpandableBody(this@DataTable, item, job)
            })
        } else {
            tbody {
                attr("role", "rowgroup")
                this@DataTable.itemStore.visible.renderEach({ this@DataTable.itemStore.idProvider(it) }, { item ->
                    tr {
                        renderCells(this@DataTable, item, "", null)
                    }
                })
            }
        }
    }
}

// public fun <T> Seq<T>.shift(amount: Int): Seq<T> = Seq(this.data.map { patch ->
//    when (patch) {
//        is Patch.Insert -> patch.copy(index = patch.index + amount)
//        is Patch.InsertMany -> patch.copy(index = patch.index + amount)
//        is Patch.Delete -> patch.copy(start = patch.start + amount)
//        is Patch.Move -> patch.copy(from = patch.from + amount, to = patch.to + amount)
//    }
//})

/**
 * A container to add a caption above the data table.
 */
public class DataTableCaption internal constructor(id: String?, baseClass: String?, job: Job) :
    Caption(id = id, baseClass = baseClass, job)

internal class DataTableExpandableBody<T>(dataTable: DataTable<T>, item: T, job: Job) : TBody(job = job) {
    private val ces: CollapseExpandStore = CollapseExpandStore()

    init {
        val expandableContentId = Id.unique(ComponentType.DataList.id, "ec")
        attr("role", "rowgroup")
        classMap(ces.data.map { mapOf("expanded".modifier() to it) })
        tr {
            renderCells(dataTable, item, expandableContentId, this@DataTableExpandableBody.ces)
        }
        val toggleColumn = dataTable.columns.toggleColumn
        if (toggleColumn != null) {
            tr(baseClass = "table".component("expandable-row")) {
                attr("role", "row")
                attr("hidden", this@DataTableExpandableBody.ces.data.map { !it })
                classMap(this@DataTableExpandableBody.ces.data.map { mapOf("expanded".modifier() to it) })
                if (toggleColumn.fullWidth) {
                    td {
                        renderExpandableContent(toggleColumn, item, dataTable.columns.size, expandableContentId)
                    }
                } else {
                    if (dataTable.columns.hasToggle) {
                        td {}
                    }
                    if (dataTable.columns.hasSelect) {
                        td {}
                    }
                    td {
                        renderExpandableContent(
                            toggleColumn,
                            item,
                            dataTable.columns.dataColumns.size,
                            expandableContentId
                        )
                    }
                    td {
                        if (dataTable.columns.actionColumns.size > 1) {
                            domNode.colSpan = dataTable.columns.actionColumns.size
                        }
                    }
                }
            }
        } else {
            console.error("No pfDataTableToggleColumn() defined for ${dataTable.domNode.debug()}")
        }
    }
}

internal fun <T> Tr.renderCells(
    dataTable: DataTable<T>,
    item: T,
    expandableContentId: String,
    ces: CollapseExpandStore?,
) {
    attr("role", "row")
    val itemId = dataTable.itemStore.idProvider(item)
    attr("rowId", "$itemId-row")
    dataTable.columns.forEach { column ->
        when (column) {
            is ToggleColumn<T> -> {
                if (dataTable.columns.hasToggle) {
                    td(
                        id = column.id,
                        baseClass = classes("table".component("toggle"), column.baseClass)
                    ) {
                        attr("role", "cell")
                        button(baseClass = "plain".modifier()) {
                            aria["labelledby"] = itemId
                            aria["controls"] = expandableContentId
                            aria["label"] = "Details"
                            ces?.let { store ->
                                aria["expancded"] = store.data.map { it.toString() }
                                classMap(store.data.map { mapOf("expanded".modifier() to it) })
                                clicks handledBy store.toggle
                            }
                            div(baseClass = "table".component("toggle", "icon")) {
                                icon("angle-down".fas())
                            }
                        }
                    }
                } else {
                    console.error("Illegal use of dataTableToggleColumn() for ${dataTable.domNode.debug()}")
                }
            }
            is SelectColumn<T> -> {
                td(id = column.id, baseClass = classes("table".component("check"), column.baseClass)) {
                    attr("role", "cell")
                    if (column.selectionMode == MULTIPLE_ALL || column.selectionMode == MULTIPLE) {
                        input {
                            type("checkbox")
                            name("$itemId-check")
                            aria["labelledby"] = itemId
                            checked(dataTable.itemStore.data.map { it.isSelected(item) })
                            changes.states().map { Pair(item, it) } handledBy dataTable.itemStore.select
                        }
                    } else if (column.selectionMode == SINGLE) {
                        input {
                            type("radio")
                            name(RADIO_GROUP_NAME) // same name for all radios == radio group
                            aria["labelledby"] = itemId
                            checked(dataTable.itemStore.data.map { it.isSelected(item) })
                            changes.states().map { item } handledBy dataTable.itemStore.selectItem
                        }
                    }
                }
            }
            is DataColumn<T> -> {
                td {
                    attr("role", "cell")
                    domNode.dataset["label"] = column.text
                    column.cellDisplay(this, item)
                }
            }
            is ActionColumn<T> -> {
                td(baseClass = classes("table".component("action"), column.baseClass)) {
                    attr("role", "cell")
                    column.display(this, item)
                }
            }
        }
    }
}

internal fun <T> Td.renderExpandableContent(
    toggleColumn: ToggleColumn<T>,
    item: T,
    colspan: Int,
    expandableContentId: String
) {
    attr("colspan", colspan.toString())
    if (toggleColumn.noPadding) {
        domNode.classList += "no-padding".modifier()
    }
    div(
        id = expandableContentId,
        baseClass = classes("table".component("expandable", "row", "content"), toggleColumn.baseClass)
    ) {
        toggleColumn.display(this, item)
    }
}

// ------------------------------------------------------ column

/**
 * Builder to add [Column]s when creating a [DataTable].
 *
 * This class is not related to a DOM element, but is used as a container in the DSL to add columns.
 */
public class Columns<T>(internal val dataTable: DataTable<T>) : Iterable<Column<T>> {
    private val columns: MutableList<Column<T>> = mutableListOf()

    internal val hasToggle: Boolean
        get() = columns.any { it is ToggleColumn<T> }

    internal val toggleColumn: ToggleColumn<T>?
        get() = columns.filterIsInstance<ToggleColumn<T>>().firstOrNull()

    internal val hasSelect: Boolean
        get() = columns.any { it is SelectColumn<T> }

    internal val dataColumns: List<DataColumn<T>>
        get() = columns.filterIsInstance<DataColumn<T>>()

    internal val actionColumns: List<ActionColumn<T>>
        get() = columns.filterIsInstance<ActionColumn<T>>()

    internal val size: Int
        get() = columns.size

    internal fun add(column: Column<T>) {
        columns.add(column)
    }

    override fun iterator(): Iterator<Column<T>> = columns.iterator()
}

/**
 * Base class for all columns available for the [DataTable].
 */
public sealed class Column<T>(
    internal val columns: Columns<T>,
    internal val id: String?,
    internal val baseClass: String?
)

/**
 * Column to show the actual data of an item in the [ItemStore]. Use this column any number of times to render the properties of the items.
 *
 * The column uses display functions for the header and cells. One of the data columns should use the [ItemStore.idProvider] to assign an element ID. This ID is referenced by various [ARIA labelledby](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute) attributes.
 *
 * If you want to make the column sortable, use one of the `sortInfo()` function to specify a [Comparator].
 *
 * @sample DataTableSamples.dataColumns
 */
public class DataColumn<T>(
    columns: Columns<T>,
    id: String?,
    baseClass: String?,
    internal val text: String
) : Column<T>(columns, id, baseClass), WithIdProvider<T> by columns.dataTable.itemStore {

    internal var cellDisplay: ComponentDisplay<Td, T> = { +it.toString() }
    internal var headerDisplay: (Th.() -> Unit)? = null
    internal var sortInfo: SortInfo<T>? = null

    /**
     * Defines how the cell should be rendered.
     */
    public fun cellDisplay(display: ComponentDisplay<Td, T>) {
        this.cellDisplay = display
    }

    /**
     * Defines how the header should be rendered.
     */
    public fun headerDisplay(display: (Th.() -> Unit)?) {
        this.headerDisplay = display
    }

    /**
     * Assigns a [SortInfo] to this column. The header will contains controls to sort the column according to the provided [Comparator].
     */
    public fun sortInfo(id: String, text: String, comparator: Comparator<T>) {
        this.sortInfo = SortInfo(id, text, comparator)
    }

    /**
     * Assigns a [SortInfo] to this column. The header will contains controls to sort the column according to the provided [Comparator].
     */
    public fun sortInfo(sortInfo: SortInfo<T>) {
        this.sortInfo = sortInfo
    }
}

/**
 * Column to add a checkbox or a radio button which you can use to (de)select rows. Depending on the value of [DataTableSelection] a checkbox or a radio button is used to select rows.
 *
 * The selection is bound to the selection state of the [ItemStore].
 *
 * You can use the [ItemStore] to track the selection of an item.
 *
 * @sample DataTableSamples.selects
 */
public class SelectColumn<T>(
    columns: Columns<T>,
    id: String?,
    baseClass: String?,
    internal val selectionMode: DataTableSelection
) : Column<T>(columns, id, baseClass)

/**
 * Column to add an expandable row below each normal row. Use this column once as the first column. The expandable content is rendered by a [display] function.
 */
public class ToggleColumn<T>(
    columns: Columns<T>,
    id: String?,
    baseClass: String?,
    internal val fullWidth: Boolean,
    internal val noPadding: Boolean,
    internal val display: ComponentDisplay<Div, T>
) : Column<T>(columns, id, baseClass)

/**
 * Column to add buttons, dropdowns or other action-like elements. Use this column any number of times to render the action element. It's up to you whether you use one action column to render multiple action elements or multiple action columns with one action element.
 *
 * This column should be placed after the data columns.
 */
public class ActionColumn<T>(
    columns: Columns<T>,
    id: String?,
    baseClass: String?,
    internal val display: ComponentDisplay<Td, T>
) : Column<T>(columns, id, baseClass)
