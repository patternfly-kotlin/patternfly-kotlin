@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Align
import org.patternfly.ButtonVariant.link
import org.patternfly.ButtonVariant.primary
import org.patternfly.ButtonVariant.secondary
import org.patternfly.actionList
import org.patternfly.dropdown
import org.patternfly.pushButton

internal class ActionListSample {

    fun items() {
        render {
            actionList {
                item {
                    pushButton(primary) { +"Next" }
                }
                item {
                    pushButton(secondary) { +"Back" }
                }
                item {
                    dropdown(align = Align.RIGHT) {
                        toggle { kebab() }
                        item("Item 1")
                        item("Disabled Item") {
                            disabled(true)
                        }
                        separator()
                        item("Separated Item")
                    }
                }
            }
        }
    }

    fun mixed() {
        render {
            actionList {
                group {
                    item {
                        pushButton(primary) { +"Next" }
                    }
                    item {
                        pushButton(secondary) { +"Back" }
                    }
                }
                item {
                    pushButton(link) { +"Cancel" }
                }
            }
        }
    }
}
