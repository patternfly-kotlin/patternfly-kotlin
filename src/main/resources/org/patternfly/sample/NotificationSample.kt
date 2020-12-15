package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.dropdown
import org.patternfly.item
import org.patternfly.items
import org.patternfly.textToggle

internal interface NotificationSample {

    fun add() {
        render {
            dropdown<Int> {
                textToggle { +"1, 2 or 3" }
                items {
                    (1..3).forEach { item(it) }
                    store.selects handledBy Notification.add { item ->
                        info("You selected $item")
                    }
                }
            }
        }
    }
}
