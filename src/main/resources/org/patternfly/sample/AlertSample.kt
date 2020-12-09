package org.patternfly.sample

import dev.fritz2.dom.html.render
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

internal interface AlertSample {

    fun standaloneAlert() {
        render {
            alert(INFO, "Alert title") {
                alertDescription { +"Lorem ipsum dolor sit amet." }
                alertActions {
                    pushButton(inline, link) { +"View details" }
                    pushButton(inline, link) { +"Ignore" }
                }
            }
        }
    }

    fun inlineAlertGroup() {
        render {
            alertGroup {
                alert(INFO, "Just saying.", inline = true)
                alert(SUCCESS, "Well done!", inline = true)
                alert(WARNING, "Really?", inline = true)
                alert(DANGER, "You're in trouble!", inline = true)
            }
        }
    }

    fun description() {
        render {
            alert(INFO, "Alert title") {
                alertDescription { +"Lorem ipsum dolor sit amet." }
            }
        }
    }

    fun actions() {
        render {
            alert(INFO, "Alert title") {
                alertActions {
                    pushButton(inline, link) { +"View details" }
                    pushButton(inline, link) { +"Ignore" }
                }
            }
        }
    }

    fun closes() {
        render {
            alert(INFO, "Close me", closable = true) {
                closes handledBy Notification.info("You did it!")
            }
        }
    }
}