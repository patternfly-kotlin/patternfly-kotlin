package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import org.patternfly.Color
import org.patternfly.Color.BLUE
import org.patternfly.Color.GREY
import org.patternfly.Severity.INFO
import org.patternfly.labelGroup
import org.patternfly.notification

internal class LabelGroupSample {

    fun staticItems() {
        render {
            labelGroup {
                +"Category"
                label(GREY) { +"Label one" }
                label(BLUE) { +"Label two" }
                for (i in 3..10) {
                    label(Color.values().random()) {
                        +"Label #$i"
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
            labelGroup {
                +"Category"
                labels(store, { it.id }) { demo ->
                    label(GREY, demo.name)
                }
            }
        }
    }

    fun close() {
        render {
            labelGroup {
                label(GREY) { +"Label one" }
                label(Color.GREEN) {
                    +"Label two"
                    closable(true)
                    events {
                        closes handledBy notification(INFO, "Bye, bye label!")
                    }
                }
                closable(true)
                events {
                    closes handledBy notification(INFO, "Bye, bye label group!")
                }
            }
        }
    }
}
