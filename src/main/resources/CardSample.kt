@file:Suppress("DuplicatedCode")

package org.patternfly

import dev.fritz2.dom.html.render
import org.patternfly.Align.RIGHT

internal interface CardSample {

    fun card() {
        render {
            card {
                cardHeader {
                    cardHeaderMain {
                        img { src("./logo.svg") }
                    }
                    cardActions {
                        dropdown<String>(align = RIGHT) {
                            kebabToggle()
                            items {
                                item("Item 1")
                                item("Disabled Item") {
                                    disabled = true
                                }
                                separator()
                                item("Separated Item")
                            }
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

    fun cardHeaderMain() {
        render {
            card {
                cardHeader {
                    cardHeaderMain {
                        img { src("./logo.svg") }
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
                    cardActions {
                        dropdown<String>(align = RIGHT) {
                            kebabToggle()
                            items {
                                item("Item 1")
                                item("Disabled Item") { disabled = true }
                                separator()
                                item("Separated Item")
                            }
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
}
