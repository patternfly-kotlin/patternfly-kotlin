@file:Suppress("SpellCheckingInspection", "DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.Severity
import org.patternfly.accordion
import org.patternfly.notification

internal interface AccordionSample {

    fun accordion() {
        render {
            accordion {
                item {
                    title("Item one")
                    content {
                        p { +"Lorem ipsum dolor sit amet." }
                    }
                }
                item {
                    expanded(true)
                    title("Item two")
                    content {
                        p { +"Phasellus pretium est a porttitor vehicula." }
                    }
                    expanded.data handledBy notification { expanded ->
                        severity(Severity.INFO)
                        title("Expanded: $expanded")
                    }
                }
                item {
                    title("Item three")
                    content {
                        p { +"Quisque vel commodo urna." }
                    }
                }
            }
        }
    }
}
