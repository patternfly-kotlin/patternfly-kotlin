package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT
import org.patternfly.ItemStore
import org.patternfly.card
import org.patternfly.cardActions
import org.patternfly.cardBody
import org.patternfly.cardCheckbox
import org.patternfly.cardHeader
import org.patternfly.cardTitle
import org.patternfly.cardView
import org.patternfly.dropdown
import org.patternfly.item
import org.patternfly.items
import org.patternfly.kebabToggle

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
