@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ButtonVariation.primary
import org.patternfly.ButtonVariation.secondary
import org.patternfly.ItemStore
import org.patternfly.Notification
import org.patternfly.dataList
import org.patternfly.dataListAction
import org.patternfly.dataListCell
import org.patternfly.dataListCheckbox
import org.patternfly.dataListContent
import org.patternfly.dataListControl
import org.patternfly.dataListExpandableContent
import org.patternfly.dataListItem
import org.patternfly.dataListRow
import org.patternfly.dataListToggle
import org.patternfly.pushButton

internal interface DataListSample {

    fun dataList() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemStore<Demo> { it.id }
            dataList(store) {
                display { demo ->
                    dataListItem(demo) {
                        dataListRow {
                            dataListControl {
                                dataListToggle()
                                dataListCheckbox()
                            }
                            dataListContent {
                                dataListCell(id = itemId(demo)) {
                                    +demo.name
                                }
                            }
                            dataListAction {
                                pushButton(primary) { +"Edit" }
                                pushButton(secondary) { +"Remove" }
                            }
                        }
                        dataListExpandableContent {
                            +"More details about ${demo.name}"
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

    fun expanded() {
        render {
            dataList<String> {
                display { item ->
                    dataListItem(item) {
                        expanded.data handledBy Notification.add { expanded ->
                            info("Expanded state of $item: $expanded.")
                        }
                        dataListRow {
                            dataListControl { dataListToggle() }
                            dataListContent { dataListCell { +item } }
                        }
                        dataListExpandableContent {
                            +"More details about $item"
                        }
                    }
                }
            }
        }
    }

    fun selects() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemStore<Demo> { it.id }
            store.selection handledBy Notification.add { selection ->
                default("Selection: $selection.")
            }

            dataList(store) {
                display { demo ->
                    dataListItem(demo) {
                        dataListRow {
                            dataListControl { dataListCheckbox() }
                            dataListContent { dataListCell { +demo.name } }
                        }
                    }
                }
            }
        }
    }
}
