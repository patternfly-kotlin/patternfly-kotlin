@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.dropdown
import org.patternfly.fas
import org.patternfly.notification
import org.patternfly.util

internal class DropdownSample {

    fun staticEntries() {
        render {
            dropdown {
                toggle { text("Choose one") }
                item("Item 1") {
                    selected(true)
                }
                item("Item 2") {
                    icon("user".fas())
                    description("Item description")
                }
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
                    item("Item 2") {
                        events {
                            clicks handledBy notification(INFO, "Click on last item of last group")
                        }
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
            dropdown {
                toggle { text("Choose one") }
                items(store, { it.id }) { demo ->
                    item(demo.name)
                }
            }
        }
    }

    fun customEntries() {
        render {
            dropdown {
                toggle { text("Choose one") }
                item("Foo") {
                    description("Description")
                }
                item("Bar") {
                    icon("user".fas())
                    description("Description")
                }
                item("") {
                    content(id = "custom-id", baseClass = "my-md".util()) {
                        +"Custom title"
                    }
                }
            }
        }
    }

    fun excos() {
        render {
            dropdown {
                toggle { text("Choose one") }
                item("Foo")
                item("Bar")
                events {
                    excos handledBy notification(INFO) { expanded ->
                        +"Expanded state of dropdown: $expanded"
                    }
                }
            }
        }
    }

    fun textToggle() {
        render {
            dropdown {
                toggle { text("Text") }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun iconToggle() {
        render {
            dropdown {
                toggle { icon("user".fas()) }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun kebabToggle() {
        render {
            dropdown {
                toggle { kebab() }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun badgeToggle() {
        render {
            dropdown {
                toggle { badge(5) }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun checkboxToggle() {
        render {
            dropdown {
                toggle { checkbox("Text") }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun actionToggle() {
        render {
            dropdown {
                toggle {
                    action("Text") {
                        events {
                            clicks handledBy notification(INFO, "Action clicked")
                        }
                    }
                }
                item("Foo")
                item("Bar")
            }
        }
    }

    fun imgToggle() {
        render {
            dropdown {
                toggle {
                    img(title = "Text", src = "./logo.svg")
                }
                item("Foo")
                item("Bar")
            }
        }
    }
}
