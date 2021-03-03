package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.dom.states
import org.patternfly.Notification
import org.patternfly.switch

internal interface SwitchSample {

    fun switch() {
        render {
            switch {
                switch {
                    label("Message when on")
                    labelOff("Message when off")
                }
            }
        }
    }

    fun input() {
        render {
            switch {
                label("Message when on")
                labelOff("Message when off")
                input.changes.states() handledBy Notification.add {
                    info("Switch is ${if (it) "" else "not"} checked")
                }
            }
        }
    }
}
