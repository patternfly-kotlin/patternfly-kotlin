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
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.set

// ------------------------------------------------------ dsl

public fun <T> RenderContext.dataTable(
    itemStore: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: DataTable<T>.() -> Unit = {}
): DataTable<T> = register(DataTable(itemStore, id = id, baseClass = baseClass, job), content)

public fun <T> DataTable<T>.dataTableCaption(
    id: String? = null,
    baseClass: String? = null,
    content: DataTableCaption.() -> Unit = {}
): DataTableCaption = register(DataTableCaption(id = id, baseClass = baseClass, job), content)

public fun <T> DataTable<T>.dataTableColumns(block: Columns<T>.() -> Unit) {
    columns.apply(block)
    renderTable()
}

public fun <T> Columns<T>.dataTableColumn(label: String, block: DataColumn<T>.() -> Unit) {
    val column = DataColumn<T>(label).apply(block)
    add(column)
}

public fun <T> Columns<T>.dataTableSimpleColumn(label: String, display: ComponentDisplay<Td, T>) {
    add(DataColumn(label, cellDisplay = display))
}

public fun <T> Columns<T>.sataTableSelectColumn(selectAll: Boolean = false, baseClass: String? = null) {
    add(SelectColumn(selectAll, baseClass))
}

public fun <T> Columns<T>.dataTableToggleColumn(
    fullWidth: Boolean = false,
    noPadding: Boolean = false,
    baseClass: String? = null,
    display: ComponentDisplay<Div, T>
) {
    add(ToggleColumn(fullWidth, noPadding, baseClass, display))
}

public fun <T> Columns<T>.dataTableActionColumn(baseClass: String? = null, display: ComponentDisplay<Td, T>) {
    add(ActionColumn(baseClass, display))
}

// ------------------------------------------------------ tag

/**
 * PatternFly [table](https://www.patternfly.org/v4/components/table/design-guidelines) component.
 *
 * A table is used to display large data sets that can be easily laid out in a simple grid with column headers. The table uses a [display] function to render the items in the ItemStore.
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
    internal val columns: Columns<T> = Columns()

    init {
        attr("role", "grid")
        markAs(ComponentType.DataTable)
    }

    public fun display(display: Columns<T>.(T) -> Unit) {
        display(columns, )
        renderTable()
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
                            if (column.selectAll) {
                                td(baseClass = "table".component("check")) {
                                    attr("role", "cell")
                                    input {
                                        domNode.type = "checkbox"
                                        aria["label"] = "Select all"
                                        changes.states().filter { !it }
                                            .map { Unit } handledBy this@DataTable.itemStore.selectNone
                                        changes.states().filter { it }
                                            .map { Unit } handledBy this@DataTable.itemStore.selectAll
                                    }
                                }
                            } else {
                                td {}
                            }
                        }
                        is DataColumn<T> -> {
                            th(id = column.headerId, baseClass = classes {
                                +column.headerBaseClass
                                +("table".component("sort") `when` (column.sortInfo != null))
                            }) {
                                attr("scope", "col")
                                attr("role", "columnheader")
                                if (column.sortInfo != null) {
                                    classMap(this@DataTable.itemStore.data.map {
                                        mapOf("selected".modifier() to (column.sortInfo!!.id == it.sortInfo?.id))
                                    })
                                    attr("aria-sort", this@DataTable.itemStore.data.map {
                                        if (it.sortInfo != null && it.sortInfo.id == column.sortInfo?.id) {
                                            if (it.sortInfo.ascending) "ascending" else "descending"
                                        } else "none"
                                    })
                                }
                                if (column.headerDisplay != null) {
                                    column.headerDisplay!!.invoke(this)
                                } else {
                                    if (column.sortInfo != null) {
                                        button(baseClass = "table".component("button")) {
                                            clicks.map { column.sortInfo!! } handledBy this@DataTable.itemStore.sortOrToggle
                                            span(baseClass = "table".component("text")) {
                                                +column.label
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
                                        +column.label
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
            // use shift(1) to keep the thead at index 0
            itemStore.visible.renderEach({ itemStore.identifier(it) }, { item ->
                DataTableExpandableBody(this@DataTable, item, job)
            })
        } else {
            tbody {
                attr("role", "rowgroup")
                this@DataTable.itemStore.visible.renderEach({ this@DataTable.itemStore.identifier(it) }, { item ->
                    tr {
                        renderCells(this@DataTable, item, "", null)
                    }
                })
            }
        }
    }
}

public class DataTableCaption internal constructor(id: String?, baseClass: String?, job: Job) :
    Caption(id = id, baseClass = baseClass, job)

internal class DataTableExpandableBody<T>(dataTable: DataTable<T>, item: T, job: Job) : TBody(job = job) {
    private val expanded: CollapseExpandStore = CollapseExpandStore()

    init {
        val expandableContentId = Id.unique(ComponentType.DataList.id, "ec")
        attr("role", "rowgroup")
        classMap(expanded.data.map { mapOf("expanded".modifier() to it) })
        tr {
            renderCells(dataTable, item, expandableContentId, this@DataTableExpandableBody.expanded)
        }
        val toggleColumn = dataTable.columns.toggleColumn
        if (toggleColumn != null) {
            tr(baseClass = "table".component("expandable-row")) {
                attr("role", "row")
                attr("hidden", this@DataTableExpandableBody.expanded.data.map { !it })
                classMap(this@DataTableExpandableBody.expanded.data.map { mapOf("expanded".modifier() to it) })
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
    val itemId = dataTable.itemStore.identifier(item)
    attr("rowId", "$itemId-row")
    dataTable.columns.forEach { column ->
        when (column) {
            is ToggleColumn -> {
                if (dataTable.columns.hasToggle) {
                    td(baseClass = classes("table".component("toggle"), column.baseClass)) {
                        attr("role", "cell")
                        button(baseClass = "plain".modifier()) {
                            aria["labelledby"] = itemId
                            aria["controls"] = expandableContentId
                            aria["label"] = "Details"
                            ces?.let { store ->
                                attr("aria-expanded", store.data.map { it.toString() })
                                classMap(store.data.map { mapOf("expanded".modifier() to it) })
                                clicks handledBy store.toggle
                            }
                            div(baseClass = "table".component("toggle", "icon")) {
                                icon("angle-down".fas())
                            }
                        }
                    }
                } else {
                    console.error("Illegal use of pfDataTableToggleColumn() for ${dataTable.domNode.debug()}")
                }
            }
            is SelectColumn -> {
                td(baseClass = classes("table".component("check"), column.baseClass)) {
                    attr("role", "cell")
                    input {
                        domNode.type = "checkbox"
                        checked(dataTable.itemStore.data.map { it.isSelected(item) })
                        changes.states().map { Pair(item, it) } handledBy dataTable.itemStore.select
                    }
                }
            }
            is DataColumn -> {
                td(baseClass = column.cellBaseClass) {
                    if (column.hasId) {
                        domNode.id = itemId
                    }
                    attr("role", "cell")
                    domNode.dataset["label"] = column.label
                    val content = column.cellDisplay(item)
                    content(this)
                }
            }
            is ActionColumn -> {
                td(baseClass = classes("table".component("action"), column.baseClass)) {
                    attr("role", "cell")
                    val content = column.display(item)
                    content(this)
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
        val content = toggleColumn.display(item)
        content(this)
    }
}

// ------------------------------------------------------ column, row and cell

public class Columns<T> : Iterable<Column<T>> {
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

public sealed class Column<T>

public class DataColumn<T>(
    public val label: String,
    public var hasId: Boolean = false,
    public var sortInfo: SortInfo<T>? = null,
    public var headerId: String? = null,
    public var headerBaseClass: String? = null,
    public var headerDisplay: (Th.() -> Unit)? = null,
    public var cellBaseClass: String? = null,
    public var cellDisplay: ComponentDisplay<Td, T> = { { !"Please render your item here" } },
    // TODO configure help: tooltip, popover, custom
) : Column<T>() {

    public fun cellDisplay(display: ComponentDisplay2<Td, T>) {

    }
}

public class SelectColumn<T>(public val selectAll: Boolean, public var baseClass: String? = null) : Column<T>()

public class ToggleColumn<T>(
    public val fullWidth: Boolean,
    public val noPadding: Boolean,
    public var baseClass: String? = null,
    public var display: ComponentDisplay<Div, T> = { { !"Please render your expandable content here" } }
) : Column<T>()

public class ActionColumn<T>(
    public var baseClass: String? = null,
    public val display: ComponentDisplay<Td, T> = { { !"Please render your actions here" } }
) : Column<T>()
