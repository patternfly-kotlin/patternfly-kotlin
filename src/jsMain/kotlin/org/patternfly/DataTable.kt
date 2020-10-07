package org.patternfly

import dev.fritz2.binding.Patch
import dev.fritz2.binding.Seq
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Caption
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TBody
import dev.fritz2.dom.html.Table
import dev.fritz2.dom.html.Td
import dev.fritz2.dom.html.Th
import dev.fritz2.dom.html.Tr
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.set

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDataTable(
    itemStore: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: DataTable<T>.() -> Unit = {}
): DataTable<T> = register(DataTable(itemStore, id = id, baseClass = baseClass), content)

fun <T> DataTable<T>.pfDataTableCaption(
    id: String? = null,
    baseClass: String? = null,
    content: DataTableCaption.() -> Unit = {}
): DataTableCaption = register(DataTableCaption(id = id, baseClass = baseClass), content)

fun <T> DataTable<T>.pfDataTableColumns(block: Columns<T>.() -> Unit) {
    columns.apply(block)
    renderTable()
}

fun <T> Columns<T>.pfDataTableColumn(label: String, block: DataColumn<T>.() -> Unit) {
    val column = DataColumn<T>(label).apply(block)
    add(column)
}

fun <T> Columns<T>.pfDataTableSimpleColumn(label: String, display: ComponentDisplay<Td, T>) {
    add(DataColumn(label, cellDisplay = display))
}

fun <T> Columns<T>.pfDataTableSelectColumn(selectAll: Boolean = false, baseClass: String? = null) {
    add(SelectColumn(selectAll, baseClass))
}

fun <T> Columns<T>.pfDataTableToggleColumn(
    fullWidth: Boolean = false,
    noPadding: Boolean = false,
    baseClass: String? = null,
    display: ComponentDisplay<Div, T>
) {
    add(ToggleColumn(fullWidth, noPadding, baseClass, display))
}

fun <T> Columns<T>.pfDataTableActionColumn(baseClass: String? = null, display: ComponentDisplay<Td, T>) {
    add(ActionColumn(baseClass, display))
}

// ------------------------------------------------------ tag

class DataTable<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    id: String?,
    baseClass: String?
) : PatternFlyComponent<HTMLTableElement>, Table(id = id, baseClass = classes {
    +ComponentType.DataTable
    +"grid-md".modifier()
    +baseClass
}) {
    internal val columns: Columns<T> = Columns()

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
                                    classMap = this@DataTable.itemStore.data.map {
                                        mapOf("selected".modifier() to (column.sortInfo!!.id == it.sortInfo?.id))
                                    }
                                    this@DataTable.itemStore.data.map {
                                        if (it.sortInfo != null && it.sortInfo.id == column.sortInfo?.id) {
                                            if (it.sortInfo.ascending) "ascending" else "descending"
                                        } else "none"
                                    }.bindAttr("aria-sort")
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
                                                pfIcon {
                                                    iconClass = this@DataTable.itemStore.data.map {
                                                        if (it.sortInfo != null && it.sortInfo.id == column.sortInfo?.id) {
                                                            if (it.sortInfo.ascending)
                                                                "long-arrow-alt-up".fas()
                                                            else
                                                                "long-arrow-alt-down".fas()
                                                        } else "arrows-alt-v".fas()
                                                    }
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
            val seq = itemStore.visible.each { itemStore.identifier(it) }.render { item ->
                DataTableExpandableBody(this@DataTable, item)
            }
            // we have a thead at index 0!
            val plusOneSeq = Seq(seq.data.map { patch ->
                when (patch) {
                    is Patch.Insert -> patch.copy(index = patch.index + 1)
                    is Patch.InsertMany -> patch.copy(index = patch.index + 1)
                    is Patch.Delete -> patch.copy(start = patch.start + 1)
                    is Patch.Move -> patch.copy(from = patch.from + 1, to = patch.to + 1)
                }
            })
            plusOneSeq.bind()
        } else {
            tbody {
                attr("role", "rowgroup")
                this@DataTable.itemStore.visible.each { this@DataTable.itemStore.identifier(it) }.render { item ->
                    tr {
                        renderCells(this@DataTable, item, "", null)
                    }
                }.bind()
            }
        }
    }
}

class DataTableCaption(id: String?, baseClass: String?) : Caption(id = id, baseClass = baseClass)

internal class DataTableExpandableBody<T>(dataTable: DataTable<T>, item: T) : TBody() {
    private val expanded: CollapseExpandStore = CollapseExpandStore()

    init {
        val expandableContentId = Id.unique(ComponentType.DataList.id, "ec")
        attr("role", "rowgroup")
        classMap = expanded.data.map { mapOf("expanded".modifier() to it) }
        tr {
            renderCells(dataTable, item, expandableContentId, this@DataTableExpandableBody.expanded)
        }
        val toggleColumn = dataTable.columns.toggleColumn
        if (toggleColumn != null) {
            tr(baseClass = "table".component("expandable-row")) {
                attr("role", "row")
                this@DataTableExpandableBody.expanded.data.map { !it }.bindAttr("hidden")
                classMap = this@DataTableExpandableBody.expanded.data.map { mapOf("expanded".modifier() to it) }
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
                        pfButton(baseClass = "plain".modifier()) {
                            aria["labelledby"] = itemId
                            aria["controls"] = expandableContentId
                            aria["label"] = "Details"
                            ces?.let { store ->
                                classMap = store.data.map { mapOf("expanded".modifier() to it) }
                                store.data.map { it.toString() }.bindAttr("aria-expanded")
                                clicks handledBy store.toggle
                            }
                            div(baseClass = "table".component("toggle", "icon")) {
                                pfIcon("angle-down".fas())
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
                        checked = dataTable.itemStore.data.map { it.isSelected(item) }
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

class Columns<T> : Iterable<Column<T>> {
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

sealed class Column<T>

class DataColumn<T>(
    val label: String,
    var hasId: Boolean = false,
    var sortInfo: SortInfo<T>? = null,
    var headerId: String? = null,
    var headerBaseClass: String? = null,
    var headerDisplay: (Th.() -> Unit)? = null,
    var cellBaseClass: String? = null,
    var cellDisplay: ComponentDisplay<Td, T> = { { !"Please render your item here" } },
    // TODO configure help: tooltip, popover, custom
) : Column<T>()

class SelectColumn<T>(val selectAll: Boolean, var baseClass: String? = null) : Column<T>()

class ToggleColumn<T>(
    val fullWidth: Boolean,
    val noPadding: Boolean,
    var baseClass: String? = null,
    var display: ComponentDisplay<Div, T> = { { !"Please render your expandable content here" } }
) : Column<T>()

class ActionColumn<T>(
    var baseClass: String? = null,
    val display: ComponentDisplay<Td, T> = { { !"Please render your actions here" } }
) : Column<T>()
