@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ButtonVariant.primary
import org.patternfly.ButtonVariant.secondary
import org.patternfly.ItemsStore
import org.patternfly.Severity.DEFAULT
import org.patternfly.Severity.INFO
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
import org.patternfly.notification
import org.patternfly.pushButton

internal class DataListSample {

    fun dataList() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }
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
                        expanded.data handledBy notification(INFO) { expanded ->
                            title("Expanded state of $item: $expanded.")
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

            val store = ItemsStore<Demo> { it.id }
            store.selection handledBy notification(DEFAULT) { selection ->
                title("Selection: $selection.")
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
