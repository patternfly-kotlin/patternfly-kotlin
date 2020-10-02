package org.patternfly

import dev.fritz2.binding.each
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.Caption
import dev.fritz2.dom.html.Div
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

fun <T> DataTable<T>.pfDataTableRow(block: DataTableRow<T>.() -> Unit = {}): DataTableRow<T> =
    DataTableRow<T>().apply(block)

fun <T> DataTable<T>.pfDataTableExpandableRow(
    block: DataTableExpandableRow<T>.() -> Unit = {}
): DataTableExpandableRow<T> = DataTableExpandableRow<T>().apply(block)

fun <T> DataTableRowBase<T>.pfDataTableCell(
    label: String? = null,
    id: String? = null,
    baseClass: String? = null,
    content: Td.() -> Unit = {}
) {
    cellBuilders.add(CellBuilder(label, id, baseClass, content))
}

fun <T> DataTableExpandableRow<T>.pfDataTableExpandableContent(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
) {
    expandableContentBuilder = ExpandableContentBuilder(
        id,
        classes("table".component("expandable-row", "content"), baseClass),
        content
    )
}

fun test() {
    val store = ItemStore<String> { it }
    render {
        pfDataTable(store, expandable = true) {
            pfDataTableColumns {
                pfDataTableColumn { +"First name" }
                pfDataTableColumn { +"Last name" }
                pfDataTableColumn { +"Birthday" }
            }
            display = { item ->
/*
                pfDataTableRow {
                    pfDataTableCell("Item") { +item }
                    pfDataTableCell(id = "unique") { +"Empty" }
                    pfDataTableCell(baseClass = "foo") {
                        pfButton(baseClass = "link".modifier()) { +"Click me" }
                    }
                }
*/
                pfDataTableExpandableRow {
                    pfDataTableCell("Item") { +item }
                    pfDataTableCell(id = "unique") { +"Empty" }
                    pfDataTableCell(baseClass = "foo") {
                        pfButton(baseClass = "link".modifier()) { +"Click me" }
                    }
                    pfDataTableExpandableContent(id = "some-id", baseClass = "some-class") {
                        +"Lorem ipsum"
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
    var display: (T) -> DataTableRowBase<T> = { DataTableRow() }
        set(value) {
            bindRows(value)
            field = value
        }

    init {
        attr("role", "grid")
        markAs(ComponentType.DataTable)
    }

    private fun bindRows(display: (T) -> DataTableRowBase<T>) {
        if (expandable) {
            itemStore.visible.each { itemStore.identifier(it) }.render { item ->
                when (val row = display(item)) {
                    is DataTableRow -> {
                        console.warn(
                            """
pfDataTableRow() was used for the display of a pfDataTable() with expandable=true.
Consider using pfDataTableExpandableRow() and pfDataTableExpandableContent() or set expandable=false.
Related data table is: '${this@DataTable.domNode.debug()}'""".trimIndent()
                        )
                        tbody {
                            attr("role", "rowgroup")
                            tr {
                                this@DataTable.renderRow(this, item, row)
                            }
                        }
                    }
                    is DataTableExpandableRow<T> -> {
                        tbody {
                            attr("role", "rowgroup")
                            tr {
                                this@DataTable.renderRow(this, item, row)
                            }
                            tr(baseClass = "table".component("expandable-row")) {
                                attr("role", "row")
                                td {
                                    attr("role", "cell")
                                    div(
                                        id = row.expandableContentBuilder.id,
                                        baseClass = row.expandableContentBuilder.baseClass
                                    ) {
                                        row.expandableContentBuilder.content(this)
                                    }
                                }
                            }
                        }
                    }
                }
            }.bind()
        } else {
            tbody {
                attr("role", "rowgroup")
                this@DataTable.itemStore.visible.each { this@DataTable.itemStore.identifier(it) }.render { item ->
                    when (val row = display(item)) {
                        is DataTableRow -> {
                            tr {
                                this@DataTable.renderRow(this, item, row)
                            }
                        }
                        is DataTableExpandableRow<T> -> {
                            console.warn(
                                """
pfDataTableExpandableRow() was used for the display of a pfDataTable() without expandable=true.
Any expandable content was omitted. Please use either pfDataTableRow() or set expandable=true.
Related data table is: '${this@DataTable.domNode.debug()}'""".trimIndent()
                            )
                            tr {
                                this@DataTable.renderRow(this, item, row)
                            }
                        }
                    }
                }.bind()
            }
        }
    }

    private fun renderRow(tr: Tr, item: T, row: DataTableRowBase<T>) {
        with(tr) {
            attr("role", "row")
            attr("rowId", "${this@DataTable.itemStore.identifier(item)}-row")
            row.cellBuilders.forEach { cell ->
                td(id = cell.id, baseClass = cell.baseClass) {
                    attr("role", "cell")
                    cell.label?.let { domNode.dataset["label"] = it }
                    cell.content(this)
                }
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

// ------------------------------------------------------ row and cell builders

sealed class DataTableRowBase<T> {
    internal val cellBuilders: MutableList<CellBuilder> = mutableListOf()
}

class DataTableRow<T> : DataTableRowBase<T>()

class DataTableExpandableRow<T> : DataTableRowBase<T>() {
    internal lateinit var expandableContentBuilder: ExpandableContentBuilder
}

internal data class CellBuilder(
    val label: String?,
    val id: String?,
    val baseClass: String?,
    val content: Td.() -> Unit
)

internal data class ExpandableContentBuilder(
    val id: String?,
    val baseClass: String?,
    val content: Div.() -> Unit
)