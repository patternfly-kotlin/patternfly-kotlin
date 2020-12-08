package org.patternfly

import dev.fritz2.dom.html.render

internal interface NotificationSample {

    fun add() {
        render {
            dropdown<Int> {
                textToggle { +"1, 2 or 3" }
                items {
                    (1..3).forEach { item(it) }
                    store.select handledBy Notification.add { item ->
                        info("You selected $item")
                    }
                }
            }
        }
    }
}
