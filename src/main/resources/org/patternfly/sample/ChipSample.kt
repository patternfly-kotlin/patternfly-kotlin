package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.dom.badge
import org.patternfly.dom.chip

internal interface ChipSample {

    fun chip() {
        render {
            chip { +"Chip" }
            chip(readOnly = true) { +"Read-only chip" }
            chip {
                +"With badge"
                badge { value(42) }
            }
        }
    }

    fun closes() {
        render {
            chip {
                +"Close me"
                closes handledBy Notification.info("You did it!")
            }
        }
    }
}
