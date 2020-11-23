@file:Suppress("DuplicatedCode")

package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import kotlinx.coroutines.flow.flowOf
import org.patternfly.ButtonVariation.inline
import org.patternfly.ButtonVariation.link
import org.patternfly.ButtonVariation.primary
import org.patternfly.IconPosition.ICON_FIRST
import org.patternfly.IconPosition.ICON_LAST
import org.patternfly.Severity.DANGER
import org.patternfly.Severity.INFO
import org.patternfly.Severity.SUCCESS
import org.patternfly.Severity.WARNING

internal class AlertSamples {

    fun RenderContext.alert() {
        alert(INFO, "Alert title") {
            alertDescription { +"Lorem ipsum dolor sit amet." }
            alertActions {
                pushButton(inline, link) { +"View details" }
                pushButton(inline, link) { +"Ignore" }
            }
        }
    }

    fun RenderContext.alertGroup() {
        alertGroup {
            alert(INFO, "Just saying.", inline = true)
            alert(SUCCESS, "Well done!", inline = true)
            alert(WARNING, "Really?", inline = true)
            alert(DANGER, "You're in trouble!", inline = true)
        }
    }

    fun RenderContext.description() {
        alert(INFO, "Alert title") {
            alertDescription { +"Lorem ipsum dolor sit amet." }
        }
    }

    fun RenderContext.actions() {
        alert(INFO, "Alert title") {
            alertActions {
                pushButton(inline, link) { +"View details" }
                pushButton(inline, link) { +"Ignore" }
            }
        }
    }

    fun RenderContext.closes() {
        alert(INFO, "Close me", closable = true) {
            closes handledBy Notification.info("You did it!")
        }
    }
}

internal class BadgeSamples {

    fun RenderContext.badge() {
        val values = flowOf(1, 2, 3)
        badge { +"Label" }
        badge {
            value("Label")
        }
        badge {
            value(23)
        }
        badge {
            value(values)
        }
    }
}

internal class ButtonSamples {

    fun RenderContext.pushButton() {
        pushButton { +"Button" }
    }

    fun RenderContext.linkButton() {
        linkButton {
            +"PatternFly"
            href("https://patternfly.org")
        }
    }

    fun Div.clickButton() {
        clickButton(primary) {
            +"Click me"
        } handledBy Notification.info("Score!")
    }

    fun RenderContext.buttonIcon() {
        pushButton {
            buttonIcon(ICON_FIRST, "user".fas())
            +"User"
        }
        linkButton {
            +"Wikipedia"
            href("https://en.wikipedia.org/")
            buttonIcon(ICON_LAST, "book".fas())
        }
    }
}

internal interface CardSamples {

    fun RenderContext.card() {
        card {
            cardHeader {
                cardHeaderMain {
                    img { src("./logo.svg") }
                }
                cardActions {
                    dropdown<String>(align = Align.RIGHT) {
                        toggleKebab()
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

    fun RenderContext.cardHeaderMain() {
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

    fun RenderContext.cardTitleInHeader() {
        card {
            cardHeader {
                cardActions {
                    dropdown<String>(align = Align.RIGHT) {
                        toggleKebab()
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

    fun RenderContext.cardTitleInCard() {
        card {
            cardTitle { +"Title" }
            cardBody { +"Body" }
            cardFooter { +"Footer" }
        }
    }

    fun RenderContext.multipleBodies() {
        card {
            cardTitle { +"Title" }
            cardBody { +"Body" }
            cardBody { +"Body" }
            cardBody { +"Body" }
            cardFooter { +"Footer" }
        }
    }
}

internal interface CardViewSamples {

    fun RenderContext.cardView() {
        data class Demo(val id: String, val name: String)

        val store = ItemStore<Demo> { it.id }
        render {
            cardView(store) { demo ->
                card(demo) {
                    cardTitle { +"Demo" }
                    cardBody { +demo.name }
                }
            }
        }

        store.addAll(
            listOf(
                Demo("foo", "Foo"),
                Demo("bar", "Bar")
            )
        )
    }
}

internal interface IconSamples {

    fun RenderContext.icons() {
        icon("bundle".pfIcon())
        icon("clock".far())
        icon("bars".fas())
    }
}

internal class PageSamples {

    fun RenderContext.typicalSetup() {
        val router = Router(StringRoute("#home"))
        page {
            pageHeader(id = "foo") {
                brand {
                    home("#home")
                    image("/assets/logo.svg")
                }
                horizontalNavigation(router) {
                    items {
                        navigationItem("#item1", "Item 1")
                        navigationItem("#item2", "Item 2")
                    }
                }
                tools {
                    notificationBadge()
                }
            }
            sidebar {
                verticalNavigation(router) {
                    items {
                        navigationItem("#item1", "Item 1")
                        navigationItem("#item2", "Item 2")
                    }
                }
            }
        }
    }
}