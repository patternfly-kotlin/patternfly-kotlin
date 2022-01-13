@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant
import org.patternfly.Severity.INFO
import org.patternfly.fas
import org.patternfly.notification
import org.patternfly.optionsMenu

internal class OptionsMenuSample {

    fun staticEntries() {
        render {
            optionsMenu {
                toggle { text("Choose one") }
                item("Item 1")
                item("Item 2")
                separator()
                group("Group 1") {
                    item("Item 1")
                    item("Item 2") {
                        disabled(true)
                    }
                }
                separator()
                group("Group 2") {
                    item("Item 1")
                    item("Item 2")
                }
            }
        }
    }

    fun sortOptions() {
        val columns = listOf("Name", "Date", "Size")
        val sortColumn = storeOf(columns[0])
        val sortAscending = storeOf(true)

        render {
            optionsMenu {
                toggle { text("Sort By") }
                columns.forEach { name ->
                    item(name) {
                        selected(sortColumn.data.map { it == name })
                        events {
                            clicks.map { name } handledBy sortColumn.update
                        }
                    }
                }
                separator()
                item("Ascending") {
                    selected(sortAscending.data)
                    events {
                        clicks.map { true } handledBy sortAscending.update
                    }
                }
                item("Descending") {
                    selected(sortAscending.data.map { !it })
                    events {
                        clicks.map { false } handledBy sortAscending.update
                    }
                }
            }
        }
    }

    fun dynamicEntries() {
        data class Demo(val id: String, val name: String)

        val store = storeOf(
            listOf(
                Demo("foo", "Foo"),
                Demo("bar", "Bar")
            )
        )
        render {
            optionsMenu {
                toggle { text("Choose one") }
                items(store, { it.id }) { demo ->
                    item(demo.name)
                }
            }
        }
    }

    fun excos() {
        render {
            optionsMenu {
                toggle { text("Choose one") }
                item("Foo")
                item("Bar")
                events {
                    excos handledBy notification(INFO) { expanded ->
                        +"Expanded state of options menu: $expanded"
                    }
                }
            }
        }
    }

    fun textToggle() {
        render {
            val selection = storeOf<String?>(null)
            optionsMenu {
                toggle { text("Text") }
                item("Foo") {
                    selected(selection.data.map { it == id })

                }
                item("Bar")
            }
        }
    }

    fun plainTextToggle() {
        render {
            optionsMenu {
                toggle { text("Text", ButtonVariant.plain) }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun iconToggle() {
        render {
            optionsMenu {
                toggle { icon("user".fas()) }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun kebabToggle() {
        render {
            optionsMenu {
                toggle { kebab() }
                item("Foo")
                item("Bar")
            }
        }
    }
}
