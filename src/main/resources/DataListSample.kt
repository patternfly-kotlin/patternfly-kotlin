@file:Suppress("DuplicatedCode")

package org.patternfly

import dev.fritz2.dom.html.render
import org.patternfly.ButtonVariation.primary
import org.patternfly.ButtonVariation.secondary

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
                                dataListCheck()
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

    fun ces() {
        render {
            dataList<String> {
                display { item ->
                    dataListItem(item) {
                        ces.data handledBy Notification.add { expanded ->
                            info("Expanded state of $item: $expanded.")
                        }
                        dataListRow {
                            dataListControl { dataListToggle() }
                            dataListContent { dataListCell { +item } }
                        }
                        dataListExpandableContent { +"More details about $item" }
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

            dataList(store) {
                display { demo ->
                    dataListItem(demo) {
                        dataListRow {
                            dataListControl { dataListCheck() }
                            dataListContent { dataListCell { +demo.name } }
                        }
                    }
                }
            }
        }
    }
}
