package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.Severity.DANGER
import org.patternfly.Severity.INFO
import org.patternfly.Severity.SUCCESS
import org.patternfly.Severity.WARNING
import org.patternfly.alert
import org.patternfly.alertGroup

internal interface AlertSample {

    fun staticAlertGroup() {
        render {
            alertGroup {
                alert {
                    severity(INFO)
                    title("Just saying.")
                }
                alert {
                    severity(SUCCESS)
                    title("Well done!")
                }
                alert {
                    severity(WARNING)
                    title("Really?")
                }
                alert {
                    severity(DANGER)
                    title("You're in trouble!")
                }
            }
        }
    }

    fun standaloneAlert() {
        render {
            alert {
                severity(INFO)
                title("Alert title")
                content { +"Lorem ipsum dolor sit amet." }
                action("View details") {
                    clicks handledBy Notification.info("Not yet implemented")
                }
                action("Ignore") {
                    clicks handledBy Notification.info("Not yet implemented")
                }
            }
        }
    }

    fun actions() {
        render {
            alert {
                severity(INFO)
                title("Alert title")
                action("View details")
                action("Ignore") {
                    clicks handledBy Notification.warning("Are you sure?")
                }
            }
        }
    }

    fun closes() {
        render {
            alert {
                severity(INFO)
                title("Close me")
                closable(true) {
                    clicks handledBy Notification.info("You did it!")
                }
            }
        }
    }
}
