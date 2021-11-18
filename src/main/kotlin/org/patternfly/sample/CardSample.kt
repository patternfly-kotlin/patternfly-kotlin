@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT
import org.patternfly.Severity.INFO
import org.patternfly.card
import org.patternfly.cardAction
import org.patternfly.cardBody
import org.patternfly.cardCheckbox
import org.patternfly.cardExpandableContent
import org.patternfly.cardFooter
import org.patternfly.cardHeader
import org.patternfly.cardTitle
import org.patternfly.cardToggle
import org.patternfly.dropdown
import org.patternfly.notification

internal interface CardSample {

    fun card() {
        render {
            card {
                cardHeader {
                    img { src("./logo.svg") }
                    cardAction {
                        dropdown<String>(align = RIGHT) {
                            toggle { kebab() }
                            item("Item 1")
                            item("Disabled Item") {
                                disabled(true)
                            }
                            separator()
                            item("Separated Item")
                        }
                        cardCheckbox()
                    }
                }
                cardTitle { +"Title" }
                cardBody { +"Body" }
                cardFooter { +"Footer" }
            }
        }
    }

    fun cardTitleInHeader() {
        render {
            card {
                cardHeader {
                    cardAction {
                        dropdown<String>(align = RIGHT) {
                            toggle { kebab() }
                            item("Item 1")
                            item("Disabled Item") {
                                disabled(true)
                            }
                            separator()
                            item("Separated Item")
                        }
                    }
                    cardTitle { +"Title" }
                }
                cardBody { +"Body" }
                cardFooter { +"Footer" }
            }
        }
    }

    fun cardTitleInCard() {
        render {
            card {
                cardTitle { +"Title" }
                cardBody { +"Body" }
                cardFooter { +"Footer" }
            }
        }
    }

    fun multipleBodies() {
        render {
            card {
                cardTitle { +"Title" }
                cardBody { +"Body" }
                cardBody { +"Body" }
                cardBody { +"Body" }
                cardFooter { +"Footer" }
            }
        }
    }

    fun expandable() {
        render {
            card {
                expanded.data handledBy notification(INFO) { expanded ->
                    title("Expanded state of card: $expanded.")
                }
                cardHeader {
                    cardToggle()
                    cardTitle { +"Title" }
                }
                cardExpandableContent {
                    cardBody { +"Body" }
                    cardFooter { +"Footer" }
                }
            }
        }
    }
}
