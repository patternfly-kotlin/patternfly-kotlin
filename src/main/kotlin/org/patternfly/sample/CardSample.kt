@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.map
import org.patternfly.Align.RIGHT
import org.patternfly.CardVariant.expandable
import org.patternfly.card
import org.patternfly.checkbox
import org.patternfly.dropdown

internal class CardSample {

    fun imageInHeader() {
        render {
            card {
                header {
                    main {
                        img { src("./logo.svg") }
                    }
                    actions {
                        dropdown(align = RIGHT) {
                            toggle { kebab() }
                            item("Item 1")
                            item("Disabled Item") {
                                disabled(true)
                            }
                            separator()
                            item("Separated Item")
                        }
                        checkbox("card-check", standalone = true)
                    }
                }
                title { +"Title" }
                body { +"Body" }
                footer { +"Footer" }
            }
        }
    }

    fun titleInHeader() {
        render {
            card {
                header {
                    actions {
                        dropdown(align = RIGHT) {
                            toggle { kebab() }
                            item("Item 1")
                            item("Disabled Item") {
                                disabled(true)
                            }
                            separator()
                            item("Separated Item")
                        }
                        checkbox("card-check", standalone = true)
                    }
                    title { +"Title" }
                }
                body { +"Body" }
                footer { +"Footer" }
            }
        }
    }

    fun noHeader() {
        render {
            card {
                title { +"Title" }
                body { +"Body" }
                footer { +"Footer" }
            }
        }
    }

    fun multipleBodies() {
        render {
            card {
                title { +"Title" }
                body { +"Body" }
                body { +"Body" }
                body { +"Body" }
                footer { +"Footer" }
            }
        }
    }

    fun expandableCard() {
        render {
            card(expandable) {
                header {
                    title {
                        expandedStore.data.map { "Expanded state: $it" }.renderText(into = this)
                    }
                }
                body { +"Body" }
                footer { +"Footer" }
            }
        }
    }
}
