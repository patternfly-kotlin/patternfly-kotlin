package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ButtonVariation
import org.patternfly.ButtonVariation.primary
import org.patternfly.IconPosition
import org.patternfly.Notification
import org.patternfly.buttonIcon
import org.patternfly.clickButton
import org.patternfly.fas
import org.patternfly.icon
import org.patternfly.linkButton
import org.patternfly.pushButton
import org.patternfly.util

internal interface ButtonSample {

    fun pushButton() {
        render {
            pushButton { +"Button" }
        }
    }

    fun linkButton() {
        render {
            linkButton {
                +"PatternFly"
                href("https://patternfly.org")
            }
        }
    }

    fun clickButton() {
        render {
            div {
                clickButton(primary) {
                    +"Click me"
                } handledBy Notification.info("Score!")
            }
        }
    }

    fun buttonIcon() {
        render {
            pushButton {
                buttonIcon(IconPosition.ICON_FIRST, "user".fas())
                +"User"
            }
            linkButton {
                span("font-size-4xl".util()) {
                    +"Wikipedia"
                }
                href("https://en.wikipedia.org/")
                buttonIcon(IconPosition.ICON_LAST, "book".fas())
            }
        }
    }

    fun justIcon() {
        render {
            pushButton(ButtonVariation.plain) {
                icon("user".fas())
            }
        }
    }
}
