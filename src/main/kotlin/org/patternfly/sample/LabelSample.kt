package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Color.BLUE
import org.patternfly.Color.CYAN
import org.patternfly.Color.GREEN
import org.patternfly.Color.GREY
import org.patternfly.Color.ORANGE
import org.patternfly.Color.RED
import org.patternfly.Severity.INFO
import org.patternfly.fas
import org.patternfly.label
import org.patternfly.notification

internal class LabelSample {

    fun basicLabels() {
        render {
            label(GREY, "Text")
            label(BLUE) { +"Text" }
            label(GREEN, outline = true) { +"Outline" }
            label(ORANGE, compact = true) { +"Compact" }
            label(RED) {
                icon("info-circle".fas())
                +"With icon"
            }
            label(CYAN) {
                +"With link"
                href("https://www.patternfly.org")
            }
        }
    }

    fun close() {
        render {
            label(GREY) {
                +"Label"
                closable(true)
                events {
                    closes handledBy notification(INFO, "Bye, bye!")
                }
            }
        }
    }
}
