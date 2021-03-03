@file:Suppress("SpellCheckingInspection", "DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.accordionContent
import org.patternfly.accordionDiv
import org.patternfly.accordionDl
import org.patternfly.accordionItem
import org.patternfly.accordionTitle

internal interface AccordionSample {

    fun accordionDl() {
        render {
            accordionDl {
                accordionItem {
                    accordionTitle { +"Item one" }
                    accordionContent {
                        p { +"Lorem ipsum dolor sit amet." }
                    }
                }
                accordionItem(expanded = true) {
                    accordionTitle { +"Item two" }
                    accordionContent {
                        p { +"Phasellus pretium est a porttitor vehicula." }
                    }
                }
                accordionItem {
                    accordionTitle { +"Item three" }
                    accordionContent {
                        p { +"Quisque vel commodo urna." }
                    }
                }
            }
        }
    }

    fun accordionDiv() {
        render {
            accordionDiv {
                accordionItem {
                    accordionTitle { +"Item one" }
                    accordionContent {
                        p { +"Lorem ipsum dolor sit amet." }
                    }
                }
                accordionItem(expanded = true) {
                    accordionTitle { +"Item two" }
                    accordionContent {
                        p { +"Phasellus pretium est a porttitor vehicula." }
                    }
                }
                accordionItem {
                    accordionTitle { +"Item three" }
                    accordionContent {
                        p { +"Quisque vel commodo urna." }
                    }
                }
            }
        }
    }
}
