@file:Suppress("SpellCheckingInspection", "DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.accordion
import org.patternfly.notification

internal class AccordionSample {

    fun accordion() {
        render {
            accordion<String>(singleExpand = true) {
                item("Item one") {
                    content {
                        p { +"Lorem ipsum dolor sit amet." }
                    }
                }
                item("Item two") {
                    content {
                        p { +"Phasellus pretium est a porttitor vehicula." }
                    }
                    expanded(true)
                }
                item("Item three") {
                    content {
                        p { +"Quisque vel commodo urna." }
                    }
                    events {
                        clicks handledBy notification(INFO, "Clicked!")
                        expos handledBy notification(INFO) { expanded ->
                            +"Expanded: $expanded"
                        }
                    }
                }
            }
        }
    }

    fun store() {
        val store = storeOf(
            listOf(
                "Item one" to "Lorem ipsum dolor sit amet.",
                "Item two" to "Phasellus pretium est a porttitor vehicula.",
                "Item three" to "Quisque vel commodo urna."
            )
        )

        render {
            accordion(store) {
                display { pair ->
                    item(pair) {
                        title(pair.first)
                        expanded(pair.first == "Item two")
                        content {
                            p { +pair.second }
                        }
                    }
                }
            }
        }
    }
}
