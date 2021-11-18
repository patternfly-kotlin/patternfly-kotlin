package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.dropdown
import org.patternfly.notification

internal class NotificationSample {

    fun add() {
        render {
            dropdown<Int> {
                toggle { text("1, 2 or 3") }
                (1..3).forEach { item(it) }
                events {
                    selections handledBy notification(INFO) { item ->
                        title("You've selected $item")
                    }
                }
            }
        }
    }
}
