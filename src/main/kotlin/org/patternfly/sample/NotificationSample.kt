package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.dropdown
import org.patternfly.notification

internal class NotificationSample {

    fun add() {
        render {
            dropdown {
                toggle { text("1, 2 or 3") }
                (1..3).forEach { number ->
                    item(number.toString())
                    events {
                        clicks handledBy notification(INFO, "You've selected $number")
                    }
                }
            }
        }
    }
}
