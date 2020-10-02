package org.patternfly

import dev.fritz2.binding.each
import dev.fritz2.dom.html.Caption
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Table
import dev.fritz2.dom.html.Td
import dev.fritz2.dom.html.Th
import dev.fritz2.dom.html.Tr
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

fun <T> Columns<T>.pfDataTableSelectColumn(selectAll: Boolean = false) {
    add(SelectColumn(selectAll))
}

fun <T> Columns<T>.pfDataTableToggleColumn(
    fullWidth: Boolean = false,
    noPadding: Boolean = false,
    baseClass: String? = null,
    display: ComponentDisplay<Div, T>
) {
    add(ToggleColumn(fullWidth, noPadding, baseClass, display))
}

fun <T> Columns<T>.pfDataTableActionColumn(display: ComponentDisplay<Td, T>) {
    add(ActionColumn(display))
}

// ------------------------------------------------------ tag

class DataTable<T> internal constructor(
    private val itemStore: ItemStore<T>,
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
                                    }
                                }
                            } else {
                                td {}
                            }
                        }
                        is DataColumn<T> -> {
                            th(id = column.headerId, baseClass = classes {
                                +column.headerBaseClass
                                +("table".component("sort") `when` (column.comparator != null))
                            }) {
                                attr("scope", "col")
                                attr("role", "columnheader")
                                if (column.comparator != null) {
                                    aria["sort"] = "none"
                                }
                                if (column.headerDisplay != null) {
                                    column.headerDisplay!!.invoke(this)
                                } else {
                                    if (column.comparator != null) {
                                        !"Comparator not yet implemented!"
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
            itemStore.visible.each { itemStore.identifier(it) }.render { item ->
                tbody {
                    attr("role", "rowgroup")
                    tr {
                        this@DataTable.renderCells(this, item)
                    }
                    val toggleColumn = this@DataTable.columns.toggleColumn
                    if (toggleColumn != null) {
                        tr(baseClass = "table".component("expandable-row")) {
                            attr("role", "row")
                            td {
                                attr("role", "cell")
                                val colspan = if (toggleColumn.fullWidth) {
                                    this@DataTable.columns.size
                                } else {
                                    this@DataTable.columns.dataColumns
                                }
                                attr("colspan", colspan.toString())
                                if (toggleColumn.noPadding) {
                                    domNode.classList += "no-padding".modifier()
                                }
                                div(baseClass = toggleColumn.baseClass) {
                                    val content = toggleColumn.display(item)
                                    content(this)
                                }
                            }
                        }
                    } else {
                        console.error(
                            """
                                No pfDataTableToggleColumn() defined for
                                ${this@DataTable.domNode.debug()}""".trimIndent()
                        )
                    }
                }
            }.bind()
        } else {
            tbody {
                attr("role", "rowgroup")
                this@DataTable.itemStore.visible.each { this@DataTable.itemStore.identifier(it) }.render { item ->
                    tr {
                        this@DataTable.renderCells(this, item)
                    }
                }.bind()
            }
        }
    }

    private fun renderCells(tr: Tr, item: T) {
        with(tr) {
            attr("role", "row")
            attr("rowId", "${this@DataTable.itemStore.identifier(item)}-row")
            this@DataTable.columns.forEach { column ->
                when (column) {
                    is ToggleColumn -> {
                        if (this@DataTable.columns.hasToggle) {
                            td(baseClass = "table".component("toggle")) {
                                attr("role", "cell")
                                pfButton(baseClass = "plain".modifier()) {
                                    div(baseClass = "table".component("toggle", "icon")) {
                                        pfIcon("angle-down".fas())
                                    }
                                }
                            }
                        } else {
                            console.error(
                                """
                                    Illegal use of pfDataTableToggleColumn() for
                                    ${this@DataTable.domNode.debug()}""".trimIndent()
                            )
                        }
                    }
                    is SelectColumn -> {
                        td(baseClass = "table".component("check")) {
                            attr("role", "cell")
                            input {
                                domNode.type = "checkbox"
                            }
                        }
                    }
                    is DataColumn -> {
                        td(baseClass = column.cellBaseClass) {
                            attr("role", "cell")
                            domNode.dataset["label"] = column.label
                            val content = column.cellDisplay(item)
                            content(this)
                        }
                    }
                    is ActionColumn -> {
                        td(baseClass = "table".component("action")) {
                            attr("role", "cell")
                            val content = column.display(item)
                            content(this)
                        }
                    }
                }
            }
        }
    }
}

class DataTableCaption(id: String?, baseClass: String?) : Caption(id = id, baseClass = baseClass)

// ------------------------------------------------------ column, row and cell

class Columns<T> : Iterable<Column<T>> {
    private val columns: MutableList<Column<T>> = mutableListOf()

    internal val hasToggle: Boolean
        get() = columns.any { it is ToggleColumn<T> }

    internal val dataColumns: Int
        get() = columns.filterIsInstance<DataColumn<T>>().size

    internal val toggleColumn: ToggleColumn<T>?
        get() = columns.filterIsInstance<ToggleColumn<T>>().firstOrNull()

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
    var comparator: Comparator<T>? = null,
    var headerId: String? = null,
    var headerBaseClass: String? = null,
    var headerDisplay: (Th.() -> Unit)? = null,
    var cellBaseClass: String? = null,
    var cellDisplay: ComponentDisplay<Td, T> = { { !"Please render your item here" } },
    // TODO configure help: tooltip, popover, custom
) : Column<T>()

class SelectColumn<T>(val selectAll: Boolean) : Column<T>()

class ToggleColumn<T>(
    val fullWidth: Boolean,
    val noPadding: Boolean,
    var baseClass: String? = null,
    var display: ComponentDisplay<Div, T> = { { !"Please render your expandable content here" } }
) : Column<T>()

class ActionColumn<T>(
    val display: ComponentDisplay<Td, T> = { { !"Please render your actions here" } }
) : Column<T>()
