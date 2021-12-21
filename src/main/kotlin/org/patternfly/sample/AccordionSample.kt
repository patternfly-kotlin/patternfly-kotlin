@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.accordion
import org.patternfly.notification

internal class AccordionSample {

    fun staticItems() {
        render {
            accordion(singleExpand = true) {
                item("Item one") {
                    content { +"Lorem ipsum dolor sit amet." }
                }
                item("Item two") {
                    content { +"Phasellus pretium est a porttitor vehicula." }
                    expanded(true)
                }
                item("Item three") {
                    content { +"Quisque vel commodo urna." }
                    events {
                        clicks handledBy notification(INFO, "Clicked!")
                        excos handledBy notification(INFO) { expanded ->
                            +"Expanded: $expanded"
                        }
                    }
                }
            }
        }
    }

    fun dynamicItems() {
        val store = storeOf(
            listOf(
                "Item one" to "Lorem ipsum dolor sit amet.",
                "Item two" to "Phasellus pretium est a porttitor vehicula.",
                "Item three" to "Quisque vel commodo urna."
            )
        )

        render {
            accordion {
                item("First item") {
                    content { +"Static item" }
                }
                items(store) { pair ->
                    item(pair.first) {
                        expanded(pair.first == "Item two")
                        content { +pair.second }
                    }
                }
                item("Last item") {
                    content { +"Static item" }
                }
            }
        }
    }
}
