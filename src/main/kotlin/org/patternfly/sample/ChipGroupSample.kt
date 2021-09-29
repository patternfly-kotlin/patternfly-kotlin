package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ChipGroupStore
import org.patternfly.Notification
import org.patternfly.chip
import org.patternfly.chipGroup
import org.patternfly.chips

internal interface ChipGroupSample {

    fun vararg() {
        render {
            chipGroup<String> {
                +"Vararg demo"
                chips("Foo", "Bar")
            }
        }
    }

    fun list() {
        render {
            chipGroup<String> {
                +"List demo"
                chips(listOf("Foo", "Bar"))
            }
        }
    }

    fun builder() {
        render {
            chipGroup<String> {
                +"Builder demo"
                chips {
                    +"Foo"
                    add("Bar")
                }
            }
        }
    }

    fun display() {
        render {
            chipGroup<String> {
                +"Display demo"
                display {
                    chip { +it.uppercase() }
                }
                chips("Foo", "Bar")
            }
        }
    }

    fun store() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ChipGroupStore<Demo> { it.id }
            chipGroup(store) {
                +"Store demo"
                display { demo ->
                    chip { +demo.name }
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

    fun closes() {
        render {
            chipGroup<String>(closable = true) {
                +"Close me"
                chips("Foo", "Bar")
                closes handledBy Notification.info("You did it!")
            }
        }
    }

    fun remove() {
        render {
            chipGroup<String> {
                +"Remove one"
                chips("Foo", "Bar")
                store.removes handledBy Notification.add { chip ->
                    info("You removed $chip.")
                }
            }
        }
    }
}
