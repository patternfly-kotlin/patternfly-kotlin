@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.filterNotNull
import org.patternfly.Notification
import org.patternfly.OptionsMenuStore
import org.patternfly.addTo
import org.patternfly.fas
import org.patternfly.group
import org.patternfly.groups
import org.patternfly.icon
import org.patternfly.iconToggle
import org.patternfly.item
import org.patternfly.items
import org.patternfly.optionsMenu
import org.patternfly.separator
import org.patternfly.textToggle
import org.patternfly.unwrap
import org.patternfly.unwrapOrNull

internal interface OptionsMenuSample {

    fun dropdownDsl() {
        render {
            optionsMenu<String> {
                textToggle { +"Choose one" }
                groups {
                    group { // group w/o title
                        item("Item 1")
                        item("Item 2") {
                            description = "Item description"
                        }
                    }
                    separator()
                    group("Group 1") {
                        item("Item 1")
                        item("Item 2") {
                            disabled = true
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
    }

    fun dropdownStore() {
        render {
            data class Demo(val id: String, val name: String)

            val store = OptionsMenuStore<Demo>().apply {
                singleSelection.unwrapOrNull() handledBy Notification.add { demo ->
                    info("You selected ${demo?.name}")
                }
                items {
                    item(Demo("foo", "Foo"))
                    item(Demo("bar", "Bar"))
                }
            }

            optionsMenu(store = store) {
                textToggle { +"Choose one" }
                display { demo -> +demo.name }
            }
        }
    }

    fun ces() {
        render {
            optionsMenu<String> {
                ces.data handledBy Notification.add { expanded ->
                    info("Expanded state of options menu: $expanded.")
                }
                textToggle { +"Choose one" }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun textToggle() {
        render {
            optionsMenu<String> {
                textToggle { +"Text" }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun plainTextToggle() {
        render {
            optionsMenu<String> {
                textToggle(plain = true) { +"Text" }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun iconToggle() {
        render {
            optionsMenu<String> {
                iconToggle { icon("user".fas()) }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun items() {
        render {
            optionsMenu<String> {
                textToggle { +"Please select" }
                items {
                    item("Item 1")
                    item("Item 2")
                }
            }
        }
    }

    fun groups() {
        render {
            optionsMenu<String> {
                textToggle { +"Please select" }
                groups {
                    group { // group w/o title
                        item("Item 1")
                        item("Item 2")
                    }
                    group("Group 1") {
                        item("Item 1")
                        item("Item 2")
                    }
                }
            }
        }
    }

    fun unwrap() {
        render {
            optionsMenu<Int> {
                textToggle { +"Favorite numbers" }
                (1..10).toList().addTo(store)
                store.singleSelection.filterNotNull().unwrap() handledBy Notification.add { number ->
                    info("You favorite number is $number")
                }
            }
        }
    }
}