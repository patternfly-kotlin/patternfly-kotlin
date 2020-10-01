package org.patternfly

import dev.fritz2.binding.each
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.Caption
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Table
import dev.fritz2.dom.html.Td
import dev.fritz2.dom.html.Th
import dev.fritz2.dom.html.Tr
import dev.fritz2.dom.html.render
import org.w3c.dom.HTMLTableElement
import org.w3c.dom.Node
import org.w3c.dom.set

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDataTable(
    itemStore: ItemStore<T>,
    expandable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: DataTable<T>.() -> Unit = {}
): DataTable<T> = register(DataTable(itemStore, expandable, id = id, baseClass = baseClass), content)

fun <T> DataTable<T>.pfDataTableCaption(
    id: String? = null,
    baseClass: String? = null,
    content: DataTableCaption.() -> Unit = {}
): DataTableCaption = register(DataTableCaption(id = id, baseClass = baseClass), content)

fun <T> DataTable<T>.pfDataTableColumns(content: DataTableColumns.() -> Unit = {}) {
    thead {
        register(DataTableColumns(), content)
    }
}

fun DataTableColumns.pfDataTableColumn(
    id: String? = null,
    baseClass: String? = null,
    content: DataTableColumn.() -> Unit = {}
): DataTableColumn = register(DataTableColumn(id = id, baseClass = baseClass), content)

fun <T> DataTable<T>.pfDataTableRows(display: (T) -> DataTableRowBase<T>) {
    registerRows(display)
}

fun <T> DataTable<T>.pfDataTableRow(block: DataTableRow<T>.() -> Unit = {}): DataTableRow<T> =
    DataTableRow<T>().apply(block)

/*
fun <T> DataTable<T>.pfDataTableExpandableRow(
    item: T,
    content: DataTableExpandableRow<T>.() -> Unit = {}
): DataTableExpandableRow<T> = DataTableExpandableRow()

fun <T> DataTableExpandableRow<T>.pfDataTableExpandableContent(
    content: DataTableExpandableRow<T>.() -> Unit = {}
) {

}
*/

fun <T> DataTableRowBase<T>.pfDataTableCell(
    label: String? = null,
    id: String? = null,
    baseClass: String? = null,
    content: DataTableCell.() -> Unit = {}
) {
    cells.add(DataTableCell(label, id = id, baseClass = baseClass, content))
}

fun test() {
    val store = ItemStore<String> { it }
    render {
        pfDataTable(store) {
            pfDataTableColumns {
                pfDataTableColumn { +"First name" }
                pfDataTableColumn { +"Last name" }
                pfDataTableColumn { +"Birthday" }
            }
            pfDataTableRows { item ->
/*
                pfDataTableExpandableRow(item) {
                    pfDataTableCell {

                    }
                    pfDataTableCell {

                    }
                    pfDataTableCell {

                    }
                    pfDataTableExpandableContent {

                    }
                }
*/
                pfDataTableRow {
                    pfDataTableCell("Item") { +item }
                    pfDataTableCell(id = "unique") { +"Empty" }
                    pfDataTableCell(baseClass = "foo") {
                        pfButton(baseClass = "link".modifier()) { +"Click me" }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------ tag

class DataTable<T> internal constructor(
    private val itemStore: ItemStore<T>,
    private val expandable: Boolean,
    id: String?,
    baseClass: String?
) : PatternFlyComponent<HTMLTableElement>, Table(id = id, baseClass = classes {
    +ComponentType.DataTable
    +"grid-md".modifier()
    +("expandable".modifier() `when` expandable)
    +baseClass
}) {
    lateinit var display: (T) -> DataTableRowBase<T>

    init {
        attr("role", "grid")
        markAs(ComponentType.DataTable)
    }

    internal fun registerRows(display: (T) -> DataTableRowBase<T>) {
        if (expandable) {
            TODO("Expandable table not yet implemented")
        } else {
            tbody {
                attr("role", "rowgroup")
                this@DataTable.itemStore.visible.each { this@DataTable.itemStore.identifier(it) }.render { item ->
                    when (val row = display(item)) {
                        is DataTableRow -> {
                            tr {
                                attr("role", "row")
                                attr("rowId", "${this@DataTable.itemStore.identifier(item)}-row")
                                row.cells.forEach { cell ->
                                    register(cell) { td ->
                                        td.attr("role", "cell")
                                        cell.label?.let { label ->
                                            td.domNode.dataset["label"] = label
                                        }
                                        cell.content(td)
                                    }
                                }
                            }
                        }
                        is DataTableExpandableRow<T> -> {
                            console.warn("Expandable row not supported for ${this@DataTable.domNode.debug()}")
                            tr {
                                !"Expandable row not supported for ${this@DataTable.domNode.debug()}"
                            }
                        }
                    }
                }.bind()
            }
        }
    }
}

class DataTableCaption(id: String?, baseClass: String?) : Caption(id = id, baseClass = baseClass)

class DataTableColumns : Tr() {
    init {
        attr("role", "row")
    }
}

class DataTableColumn(id: String?, baseClass: String?) : Th(id = id, baseClass = baseClass) {

    var label: String = "Column"
        set(value) {
            domNode.dataset["label"] = value
            field = value
        }

    init {
        attr("role", "columnheader")
        attr("scope", "column")
        domNode.dataset["label"] = label
    }

    override fun text(value: String): Node {
        domNode.dataset["label"] = value
        return super.text(value)
    }

    override fun String.unaryPlus(): Node {
        domNode.dataset["label"] = this
        return domNode.appendChild(TextNode(this).domNode)
    }
}

sealed class DataTableRowBase<T> {
    internal val cells: MutableList<DataTableCell> = mutableListOf()
}

class DataTableRow<T> : DataTableRowBase<T>()

class DataTableExpandableRow<T> : DataTableRowBase<T>()

class DataTableCell(
    internal val label: String?,
    id: String?,
    baseClass: String?,
    internal val content: DataTableCell.() -> Unit
) : Td(id = id, baseClass = baseClass)
