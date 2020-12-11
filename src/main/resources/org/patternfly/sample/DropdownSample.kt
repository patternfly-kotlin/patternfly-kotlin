package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.DropdownStore
import org.patternfly.Notification
import org.patternfly.actionToggle
import org.patternfly.checkboxToggle
import org.patternfly.customToggle
import org.patternfly.dropdown
import org.patternfly.fas
import org.patternfly.group
import org.patternfly.groups
import org.patternfly.icon
import org.patternfly.iconToggle
import org.patternfly.item
import org.patternfly.items
import org.patternfly.kebabToggle
import org.patternfly.separator
import org.patternfly.textToggle
import org.patternfly.toggleIcon
import org.patternfly.toggleImage
import org.patternfly.toggleText

internal interface DropdownSample {

    fun dropdownDsl() {
        render {
            dropdown<String> {
                textToggle { +"Choose one" }
                groups {
                    group { // w/o title
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

            val store = DropdownStore<Demo>().apply {
                select handledBy Notification.add { demo ->
                    info("You selected ${demo.name}")
                }
            }
            dropdown(store) {
                textToggle { +"Choose one" }
                display { demo -> +demo.name }
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
            dropdown<String> {
                ces.data handledBy Notification.add { expanded ->
                    info("Expanded state of dropdown: $expanded.")
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
            dropdown<String> {
                textToggle { +"Text" }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun iconToggle() {
        render {
            dropdown<String> {
                iconToggle { icon("user".fas()) }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun kebabToggle() {
        render {
            dropdown<String> {
                kebabToggle()
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun checkboxToggle() {
        render {
            dropdown<String> {
                checkboxToggle {
                    text { +"Text" }
                    checkbox {
                        checked(true)
                    }
                }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun actionToggle() {
        render {
            dropdown<String> {
                actionToggle {
                    +"Action"
                } handledBy Notification.info("Action clicked")
                items {
                    item("Foo")
                    item("Bar")
                }
            }
            dropdown<String> {
                actionToggle {
                    icon("cog".fas())
                }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun customToggle() {
        render {
            dropdown<String> {
                customToggle {
                    toggleImage {
                        img { src("./logo.svg") }
                    }
                    toggleText { +"Some text" }
                    toggleIcon()
                }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }
}