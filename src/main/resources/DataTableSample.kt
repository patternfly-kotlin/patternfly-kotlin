@file:Suppress("DuplicatedCode")

package org.patternfly

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT
import org.patternfly.ButtonVariation.plain

internal interface DataTableSample {

    fun dataTable() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemStore<Demo> { it.id }
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
                            kebabToggle()
                            items {
                                item("Action 1")
                                item("Action 2")
                                item("Remove ${demo.name}")
                            }
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
                        cellDisplay { +it.toUpperCase() }
                    }
                }
            }
        }
    }

    fun selects() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemStore<Demo> { it.id }
            store.select handledBy Notification.add { (demo, selected) ->
                default("${demo.name} selected: $selected.")
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
