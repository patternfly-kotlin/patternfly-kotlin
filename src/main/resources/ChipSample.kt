package org.patternfly

import dev.fritz2.dom.html.render

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
