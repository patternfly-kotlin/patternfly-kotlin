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
                    +"Just saying."
                }
                alert {
                    severity(SUCCESS)
                    +"Well done!"
                }
                alert {
                    severity(WARNING)
                    +"Really?"
                }
                alert {
                    severity(DANGER)
                    +"You're in trouble!"
                }
            }
        }
    }

    fun standaloneAlert() {
        render {
            alert {
                severity(INFO)
                +"Alert title"
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
                +"Alert title"
                action("View details")
                action("Ignore") {
                    clicks handledBy Notification.warning("Are you sure?")
                }
            }
        }
    }
}
