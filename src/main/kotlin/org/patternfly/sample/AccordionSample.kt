@file:Suppress("SpellCheckingInspection", "DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.accordion
import org.patternfly.accordionStore
import org.patternfly.notification

internal class AccordionSample {

    fun accordion() {
        render {
            accordion(singleExpand = true) {
                item("Item one") {
                    content {
                        p { +"Lorem ipsum dolor sit amet." }
                    }
                }
                item("Item two") {
                    expanded(true)
                    content {
                        p { +"Phasellus pretium est a porttitor vehicula." }
                    }
                }
                item("Item three") {
                    content {
                        p { +"Quisque vel commodo urna." }
                    }
                    events {
                        clicks handledBy notification(INFO, "Clicked!")
                        coexs handledBy notification(INFO) { expanded ->
                            +"Expanded: $expanded"
                        }
                    }
                }
            }
        }
    }

    fun store() {
        val store = accordionStore {
            item("Item one") {
                content {
                    p { +"Lorem ipsum dolor sit amet." }
                }
            }
            item("Item two") {
                expanded(true)
                content {
                    p { +"Phasellus pretium est a porttitor vehicula." }
                }
            }
            item("Item three") {
                content {
                    p { +"Quisque vel commodo urna." }
                }
            }
        }

        render {
            accordion(store)
        }
    }
}
