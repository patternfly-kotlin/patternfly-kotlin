@file:Suppress("SpellCheckingInspection", "DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity
import org.patternfly.accordion
import org.patternfly.notification

internal interface AccordionSample {

    fun accordion() {
        render {
            accordion {
                item {
                    +"Item one"
                    content {
                        p { +"Lorem ipsum dolor sit amet." }
                    }
                }
                item {
                    expanded(true)
                    +"Item two"
                    content {
                        p { +"Phasellus pretium est a porttitor vehicula." }
                    }
                    expanded.data handledBy notification { expanded ->
                        severity(Severity.INFO)
                        +"Expanded: $expanded"
                    }
                }
                item {
                    +"Item three"
                    content {
                        p { +"Quisque vel commodo urna." }
                    }
                }
            }
        }
    }
}
