package org.patternfly

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT

internal interface CardViewSample {

    fun cardView() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemStore<Demo> { it.id }
            cardView(store) {
                display { demo ->
                    card(demo) {
                        cardHeader {
                            cardTitle { +"Demo" }
                            cardActions {
                                dropdown<String>(align = RIGHT) {
                                    kebabToggle()
                                    items {
                                        item("Edit")
                                        item("Remove")
                                    }
                                }
                                cardCheckbox()
                            }
                        }
                        cardBody(id = itemId(demo)) { +demo.name }
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
}
