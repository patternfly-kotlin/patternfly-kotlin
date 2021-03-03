package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.badge
import org.patternfly.chip

internal interface ChipSample {

    fun basicChips() {
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
