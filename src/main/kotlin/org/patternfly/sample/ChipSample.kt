package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.chip
import org.patternfly.notification

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
                closeButton {
                    clicks handledBy notification(INFO,"Bye, bye!")
                }
            }
        }
    }
}
