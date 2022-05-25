package org.patternfly.sample

import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import org.patternfly.Align.RIGHT
import org.patternfly.CardVariant.selectable
import org.patternfly.SelectionMode
import org.patternfly.cardView
import org.patternfly.dropdown

internal class CardViewSample {

    fun cardView() {
        render {
            data class Demo(val id: String, val name: String)

            val idProvider: IdProvider<Demo, String> = { it.id }
            val selection: Store<Demo?> = storeOf(null)
            val values = storeOf(
                listOf(
                    Demo("foo", "Foo"),
                    Demo("bar", "Bar")
                )
            )

            cardView(SelectionMode.SINGLE) {
                items(values, idProvider, selection) { demo ->
                    card(selectable) {
                        header {
                            title { +"Demo" }
                            actions {
                                dropdown(align = RIGHT) {
                                    toggle { kebab() }
                                    item("Edit")
                                    item("Remove")
                                }
                            }
                            check()
                        }
                        body(id = demo.id) { +demo.name }
                    }
                }
            }
        }
    }
}
