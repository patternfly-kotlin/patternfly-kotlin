package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.dropdown
import org.patternfly.item
import org.patternfly.optionsMenu
import org.patternfly.items
import org.patternfly.textToggle

internal interface OptionsMenuSample {

    fun optionsMenu() {
        render {
            optionsMenu<String> {
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }

    fun ces() {
        render {
            optionsMenu<String> {
                ces.data handledBy Notification.add { expanded ->
                    info("Expanded state of options mneu: $expanded.")
                }
//                textToggle { +"Choose one" }
                items {
                    item("Foo")
                    item("Bar")
                }
            }
        }
    }
}