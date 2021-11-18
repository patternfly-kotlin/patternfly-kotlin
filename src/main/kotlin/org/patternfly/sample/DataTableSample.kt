@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT
import org.patternfly.ButtonVariation.plain
import org.patternfly.ItemsStore
import org.patternfly.Severity.DEFAULT
import org.patternfly.SortInfo
import org.patternfly.dataTable
import org.patternfly.dataTableActionColumn
import org.patternfly.dataTableCaption
import org.patternfly.dataTableColumn
import org.patternfly.dataTableColumns
import org.patternfly.dataTableSelectColumn
import org.patternfly.dataTableToggleColumn
import org.patternfly.dropdown
import org.patternfly.fas
import org.patternfly.icon
import org.patternfly.notification
import org.patternfly.pushButton

internal interface DataTableSample {

    fun dataTable() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }
            dataTable(store) {
                dataTableCaption { +"Demo Table" }
                dataTableColumns {
                    dataTableToggleColumn { demo ->
                        +"More details about ${demo.name}"
                    }
                    dataTableSelectColumn()
                    dataTableColumn("Id") {
                        cellDisplay { demo ->
                            span(id = itemId(demo)) { +demo.id }
                        }
                    }
                    dataTableColumn("Name") {
                        sortInfo("name", "Name") { a, b ->
                            a.name.compareTo(b.name)
                        }
                        cellDisplay { demo -> +demo.name }
                    }
                    dataTableActionColumn {
                        pushButton(plain) { icon("pencil".fas()) }
                    }
                    dataTableActionColumn { demo ->
                        dropdown<String>(align = RIGHT) {
                            toggle { kebab() }
                            item("Action 1")
                            item("Action 2")
                            item("Remove ${demo.name}")
                        }
                    }
                }
            }

            store.addAll(
                listOf(
                    Demo("foo", "Foo"),
                    Demo("bar", "Bar")
                )
            )
        }
    }

    fun dataColumns() {
        render {
            dataTable<String> {
                dataTableColumns {
                    dataTableColumn("Item") {
                        sortInfo(SortInfo("item", "Item", naturalOrder()))
                        cellDisplay {
                            span(id = itemId(it)) { +it }
                        }
                    }
                    dataTableColumn("Shout") {
                        headerDisplay { icon("volume-up") }
                        cellDisplay { +it.uppercase() }
                    }
                }
            }
        }
    }

    fun selects() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }
            store.selection handledBy notification(DEFAULT) { selection ->
                title("Selection: $selection.")
            }

            dataTable(store) {
                dataTableColumns {
                    dataTableSelectColumn()
                    dataTableColumn("Name") {
                        cellDisplay { +it.name }
                    }
                }
            }
        }
    }
}
