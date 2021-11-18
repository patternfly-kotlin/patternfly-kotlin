package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT
import org.patternfly.ItemsStore
import org.patternfly.card
import org.patternfly.cardAction
import org.patternfly.cardBody
import org.patternfly.cardCheckbox
import org.patternfly.cardHeader
import org.patternfly.cardTitle
import org.patternfly.cardView
import org.patternfly.dropdown

internal interface CardViewSample {

    fun cardView() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }
            cardView(store) {
                display { demo ->
                    card(demo) {
                        cardHeader {
                            cardTitle { +"Demo" }
                            cardAction {
                                dropdown<String>(align = RIGHT) {
                                    toggle { kebab() }
                                    item("Edit")
                                    item("Remove")
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
