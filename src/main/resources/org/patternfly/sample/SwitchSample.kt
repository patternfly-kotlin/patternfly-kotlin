package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.dom.states
import org.patternfly.AlertGroup
import org.patternfly.ButtonVariation.inline
import org.patternfly.ButtonVariation.link
import org.patternfly.Notification
import org.patternfly.Severity.DANGER
import org.patternfly.Severity.INFO
import org.patternfly.Severity.SUCCESS
import org.patternfly.Severity.WARNING
import org.patternfly.alert
import org.patternfly.alertActions
import org.patternfly.alertDescription
import org.patternfly.alertGroup
import org.patternfly.pushButton
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
