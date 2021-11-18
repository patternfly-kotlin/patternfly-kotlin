@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity.DANGER
import org.patternfly.Severity.INFO
import org.patternfly.Severity.SUCCESS
import org.patternfly.Severity.WARNING
import org.patternfly.alert
import org.patternfly.alertGroup
import org.patternfly.notification

internal class AlertSample {

    fun alertGroup() {
        render {
            alertGroup {
                alert(INFO, "Just saying.")
                alert(SUCCESS, "Well done!")
                alert(WARNING, "Really?")
                alert(DANGER, "You're in trouble!")
            }
        }
    }

    fun alert() {
        render {
            alert(INFO, "Alert title") {
                content {
                    p {
                        +"Alert title. "
                        a {
                            href("#")
                            +"This is a link."
                        }
                    }
                }
            }
        }
    }

    fun actions() {
        render {
            alert(INFO, "Alert title") {
                action("View details") {
                    clicks handledBy notification(INFO, "Here are the details...")
                }
                action(
                    {
                        +"Ignore"
                        className("your-css-class")
                    },
                    {
                        clicks handledBy notification(WARNING, "Are you sure?")
                    }
                )
            }
        }
    }
}