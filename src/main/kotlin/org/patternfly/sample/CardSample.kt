@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.map
import org.patternfly.Align.RIGHT
import org.patternfly.card
import org.patternfly.dropdown

internal class CardSample {

    fun imageInHeader() {
        render {
            card {
                header {
                    content {
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
            card {
                header {
                    toggle()
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
