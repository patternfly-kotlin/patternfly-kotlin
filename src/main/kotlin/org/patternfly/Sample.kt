@file:Suppress("DuplicatedCode", "UNUSED_VARIABLE")

package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import kotlinx.coroutines.flow.flowOf
import org.patternfly.Align.RIGHT
import org.patternfly.ButtonVariation.inline
import org.patternfly.ButtonVariation.link
import org.patternfly.ButtonVariation.plain
import org.patternfly.ButtonVariation.primary
import org.patternfly.ButtonVariation.secondary
import org.patternfly.IconPosition.ICON_FIRST
import org.patternfly.IconPosition.ICON_LAST
import org.patternfly.Severity.DANGER
import org.patternfly.Severity.INFO
import org.patternfly.Severity.SUCCESS
import org.patternfly.Severity.WARNING
import kotlin.random.Random

// ------------------------------------------------------ samples a-z

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
            span("font-size-4xl".util()) {
                +"Wikipedia"
            }
            href("https://en.wikipedia.org/")
            buttonIcon(ICON_LAST, "book".fas())
        }
    }

    fun RenderContext.justIcon() {
        pushButton(plain) {
            icon("user".fas())
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
                    dropdown<String>(align = RIGHT) {
                        dropdownKebabToggle()
                        dropdownItems {
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
                    dropdown<String>(align = RIGHT) {
                        dropdownKebabToggle()
                        dropdownItems {
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
        cardView(store) {
            display { demo ->
                card(demo) {
                    cardHeader {
                        cardTitle { +"Demo" }
                        cardActions {
                            dropdown<String>(align = RIGHT) {
                                dropdownKebabToggle()
                                dropdownItems {
                                    item("Edit")
                                    item("Remove")
                                }
                            }
                            cardCheckbox()
                        }
                    }
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

internal interface ChipSamples {

    fun RenderContext.chip() {
        chip { +"Chip" }
        chip(readOnly = true) { +"Read-only chip" }
        chip {
            +"With badge"
            badge { value(42) }
        }
    }

    fun RenderContext.closes() {
        chip {
            +"Close me"
            closes handledBy Notification.info("You did it!")
        }
    }
}

internal interface ChipGroupSamples {

    fun RenderContext.chipGroupStatic() {
        chipGroup {
            +"Static demo"
            chips {
                chip { +"Foo" }
                chip { +"Bar" }
            }
        }
    }

    fun RenderContext.chipGroupStore() {
        data class Demo(val id: String, val name: String)

        val store = ChipGroupStore<Demo> { it.id }
        chipGroup(store) {
            +"Store demo"
            display { demo ->
                chip { +demo.name }
            }
        }

        store.addAll(
            listOf(
                Demo("foo", "Foo"),
                Demo("bar", "Bar")
            )
        )
    }

    fun RenderContext.closes() {
        chipGroup(closable = true) {
            +"Close me"
            closes handledBy Notification.info("You did it!")
            chips {
                chip { +"Foo" }
                chip { +"Bar" }
            }
        }
    }
}

internal interface CSSSamples {

    fun component() {
        "card".component() // pf-c-card
        "card".component("header") // pf-c-card__header
        "card".component("header", "main") // pf-c-card__header-main
    }

    fun classesDsl() {
        val disabled = Random.nextBoolean()
        val classes: String? = classes {
            +"button".component()
            +"plain".modifier()
            +("disabled".modifier() `when` disabled)
        }
    }

    fun classesVarArg() {
        val classes: String? = classes(
            "button".component(),
            "plain".modifier(),
            "disabled".modifier()
        )
    }
}

internal interface DataListSamples {

    fun RenderContext.dataList() {
        data class Demo(val id: String, val name: String)

        val store = ItemStore<Demo> { it.id }
        dataList(store) {
            display { demo ->
                dataListItem(demo) {
                    dataListRow {
                        dataListControl {
                            dataListToggle()
                            dataListCheck()
                        }
                        dataListContent {
                            dataListCell { +demo.name }
                        }
                        dataListAction {
                            pushButton(primary) { +"Edit" }
                            pushButton(secondary) { +"Remove" }
                        }
                    }
                    dataListExpandableContent {
                        dataListExpandableContentBody {
                            +"More details about ${demo.name}"
                        }
                    }
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

internal interface DataTableSamples {

    fun RenderContext.dataTable() {
        data class Demo(val id: String, val name: String)

        val store = ItemStore<Demo> { it.id }
        dataTable(store) {
            dataTableCaption { +"Demo Data" }
            dataTableColumns {
                dataTableColumn("Name") {
                    cellDisplay { demo ->
                        +demo.name
                    }
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