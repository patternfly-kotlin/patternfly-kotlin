package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.chip

internal interface ChipSample {

    fun basicChips() {
        render {
            chip { +"Chip" }
            chip {
                +"Read-only chip"
                readOnly(true)
            }
            chip {
                +"With badge"
                badge {
                    count(42)
                }
                closable {
                    clicks handledBy Notification.info("Bye, bye!")
                }
            }
        }
    }
}
