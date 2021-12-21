package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.chipGroup
import org.patternfly.notification

internal class ChipGroupSample {

    fun staticItems() {
        render {
            chipGroup {
                +"Category"
                chip { +"Chip one" }
                chip { +"Chip two" }
                for (i in 3..10) {
                    chip(i.toString()) {
                        badge(i)
                    }
                }
            }
        }
    }

    fun dynamicItems() {
        data class Demo(val id: String, val name: String)

        val store = storeOf(
            listOf(
                Demo("foo", "Foo"),
                Demo("bar", "Bar")
            )
        )
        render {
            chipGroup {
                +"Category"
                chips(store, { it.id }) { demo ->
                    chip(demo.name)
                }
            }
        }
    }

    fun close() {
        render {
            chipGroup {
                chip { +"Chip one" }
                chip {
                    +"Chip two"
                    // chips inside a chip group are *always* closable
                    events {
                        closes handledBy notification(INFO, "Bye, bye chip!")
                    }
                }
                closable(true)
                events {
                    closes handledBy notification(INFO, "Bye, bye chip group!")
                }
            }
        }
    }
}
