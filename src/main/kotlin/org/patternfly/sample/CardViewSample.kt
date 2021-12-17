package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT
import org.patternfly.ItemsStore
import org.patternfly.legacyCard
import org.patternfly.legacyCardAction
import org.patternfly.legacyCardBody
import org.patternfly.legacyCardCheckbox
import org.patternfly.legacyCardHeader
import org.patternfly.legacyCardTitle
import org.patternfly.cardView
import org.patternfly.dropdown

internal interface CardViewSample {

    fun cardView() {
        render {
            data class Demo(val id: String, val name: String)

            val store = ItemsStore<Demo> { it.id }
            cardView(store) {
                display { demo ->
                    legacyCard(demo) {
                        legacyCardHeader {
                            legacyCardTitle { +"Demo" }
                            legacyCardAction {
                                dropdown<String>(align = RIGHT) {
                                    toggle { kebab() }
                                    item("Edit")
                                    item("Remove")
                                }
                                legacyCardCheckbox()
                            }
                        }
                        legacyCardBody(id = itemId(demo)) { +demo.name }
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
