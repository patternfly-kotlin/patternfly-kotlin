@file:Suppress("DuplicatedCode", "UNUSED_VARIABLE")

package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.elemento.Id
import dev.fritz2.lenses.IdProvider
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
                                kebabToggle()
                                items {
                                    item("Edit")
                                    item("Remove")
                                }
                            }
                            cardCheckbox()
                        }
                    }
                    cardBody(id = itemId(demo)) { +demo.name }
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

    fun RenderContext.vararg() {
        chipGroup<String> {
            +"Vararg demo"
            chips("Foo", "Bar")
        }
    }

    fun RenderContext.list() {
        chipGroup<String> {
            +"List demo"
            chips(listOf("Foo", "Bar"))
        }
    }

    fun RenderContext.builder() {
        chipGroup<String> {
            +"Builder demo"
            chips {
                +"Foo"
                add("Bar")
            }
        }
    }

    fun RenderContext.display() {
        chipGroup<String> {
            +"Display demo"
            display {
                chip { +it.toUpperCase() }
            }
            chips("Foo", "Bar")
        }
    }

    fun RenderContext.store() {
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
        chipGroup<String>(closable = true) {
            +"Close me"
            chips("Foo", "Bar")
            closes handledBy Notification.info("You did it!")
        }
    }

    fun RenderContext.remove() {
        chipGroup<String> {
            +"Remove one"
            chips("Foo", "Bar")
            store.remove handledBy Notification.add { chip ->
                info("You removed $chip.")
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
        val classes = classes {
            +"button".component()
            +"plain".modifier()
            +("disabled".modifier() `when` disabled)
        }
    }

    fun classesVararg() {
        val classes = classes(
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
                            dataListCell(id = itemId(demo)) {
                                +demo.name
                            }
                        }
                        dataListAction {
                            pushButton(primary) { +"Edit" }
                            pushButton(secondary) { +"Remove" }
                        }
                    }
                    dataListExpandableContent {
                        +"More details about ${demo.name}"
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

    fun RenderContext.ces() {
        dataList<String> {
            display { item ->
                dataListItem(item) {
                    ces.data handledBy Notification.add { expanded ->
                        info("Expanded state of $item: $expanded.")
                    }
                    dataListRow {
                        dataListControl { dataListToggle() }
                        dataListContent { dataListCell { +item } }
                    }
                    dataListExpandableContent { +"More details about $item" }
                }
            }
        }
    }

    fun RenderContext.selects() {
        data class Demo(val id: String, val name: String)

        val store = ItemStore<Demo> { it.id }
        store.select handledBy Notification.add { (demo, selected) ->
            default("${demo.name} selected: $selected.")
        }

        dataList(store) {
            display { demo ->
                dataListItem(demo) {
                    dataListRow {
                        dataListControl { dataListCheck() }
                        dataListContent { dataListCell { +demo.name } }
                    }
                }
            }
        }
    }
}

internal interface DataTableSamples {

    fun RenderContext.dataTable() {
        data class Demo(val id: String, val name: String)

        val store = ItemStore<Demo> { it.id }
        dataTable(store) {
            dataTableCaption { +"Demo Table" }
            dataTableColumns {
                dataTableToggleColumn { demo ->
                    +"More details about ${demo.name}"
                }
                dataTableSelectColumn()
                dataTableColumn("Id") {
                    cellDisplay { demo ->
                        span(id = itemId(demo)) { +demo.id }
                    }
                }
                dataTableColumn("Name") {
                    sortInfo("name", "Name") { a, b ->
                        a.name.compareTo(b.name)
                    }
                    cellDisplay { demo -> +demo.name }
                }
                dataTableActionColumn {
                    pushButton(plain) { icon("pencil".fas()) }
                }
                dataTableActionColumn { demo ->
                    dropdown<String>(align = RIGHT) {
                        kebabToggle()
                        items {
                            item("Action 1")
                            item("Action 2")
                            item("Remove ${demo.name}")
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

    fun RenderContext.dataColumns() {
        dataTable<String> {
            dataTableColumns {
                dataTableColumn("Item") {
                    sortInfo(SortInfo("item", "Item", naturalOrder()))
                    cellDisplay {
                        span(id = itemId(it)) { +it }
                    }
                }
                dataTableColumn("Shout") {
                    headerDisplay { icon("volume-up") }
                    cellDisplay { +it.toUpperCase() }
                }
            }
        }
    }

    fun RenderContext.selects() {
        data class Demo(val id: String, val name: String)

        val store = ItemStore<Demo> { it.id }
        store.select handledBy Notification.add { (demo, selected) ->
            default("${demo.name} selected: $selected.")
        }

        dataTable(store) {
            dataTableColumns {
                dataTableSelectColumn()
                dataTableColumn("Name") {
                    cellDisplay { +it.name }
                }
            }
        }
    }
}

internal interface DrawerSamples {

    fun RenderContext.drawerSetup() {
        val store = ItemStore<String>()

        drawer {
            drawerSection {
                +"Primary detail demo"
            }
            drawerContent {
                drawerBody {
                    dataList(store, selectableRows = true) {
                        display { item ->
                            dataListItem(item) {
                                dataListRow { +item }
                            }
                        }
                    }
                }
            }
            drawerPanel {
                drawerBodyWithClose {
                    h2 { +"Details of selected item" }
                }
                drawerBody {
                    store.selectItem.asText()
                }
            }
        }

        store.addAll(listOf("One", "Two", "Three"))
    }

    fun RenderContext.ces() {
        drawer {
            ces.data handledBy Notification.add { expanded ->
                info("Expanded state of drawer: $expanded.")
            }
            drawerContent {
                drawerBody { +"Drawer content" }
            }
            drawerPanel {
                drawerBodyWithClose {
                    +"Drawer panel"
                }
            }
        }
    }

    fun RenderContext.drawerContents() {
        drawer {
            drawerContent {
                drawerBody { +"Actual" }
                drawerBody { +"content" }
                drawerBody { +"goes here" }
            }
        }
    }

    fun RenderContext.drawerPanels() {
        drawer {
            drawerPanel {
                drawerBodyWithClose { +"Title" }
                drawerBody { +"additional" }
                drawerBody { +"content" }
            }
        }
        // is the same as
        drawer {
            drawerPanel {
                drawerBody {
                    drawerHead {
                        +"Title"
                        drawerActions {
                            drawerClose()
                        }
                    }
                }
                drawerBody { +"additional" }
                drawerBody { +"content" }
            }
        }
    }
}

internal interface DropdownSamples {

    fun RenderContext.dropdownDsl() {
        dropdown<String> {
            textToggle { +"Choose one" }
            groups {
                group { // w/o title
                    item("Item 1")
                    item("Item 2") {
                        description = "Item description"
                    }
                }
                group("Group 1") {
                    item("Item 1")
                    separator()
                    item("Item 2") {
                        disabled = true
                    }
                }
                group("Group 2") {
                    item("Item 1")
                    item("Item 2")
                }
            }
        }
    }

    fun RenderContext.dropdownStore() {
        data class Demo(val id: String, val name: String)

        val store = DropdownStore<Demo>()
        dropdown(store) {
            textToggle { +"Choose one" }
            display { demo -> +demo.name }
        }

        store.addAll(
            listOf(
                Demo("foo", "Foo"),
                Demo("bar", "Bar")
            )
        )
    }

    fun RenderContext.ces() {
        dropdown<String> {
            ces.data handledBy Notification.add { expanded ->
                info("Expanded state of dropdown: $expanded.")
            }
            textToggle { +"Choose one" }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    fun RenderContext.textToggle() {
        dropdown<String> {
            textToggle { +"Text" }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    fun RenderContext.iconToggle() {
        dropdown<String> {
            iconToggle { icon("user".fas()) }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    fun RenderContext.kebabToggle() {
        dropdown<String> {
            kebabToggle()
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    fun RenderContext.checkboxToggle() {
        dropdown<String> {
            checkboxToggle {
                text { +"Text" }
                checkbox {
                    checked(true)
                }
            }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    fun RenderContext.actionToggle() {
        dropdown<String> {
            actionToggle {
                +"Action"
            } handledBy Notification.info("Action clicked")
            items {
                item("Foo")
                item("Bar")
            }
        }
        dropdown<String> {
            actionToggle {
                icon("cog".fas())
            }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    fun RenderContext.customToggle() {
        dropdown<String> {
            customToggle {
                toggleImage {
                    img { src("./logo.svg") }
                }
                toggleText { +"Some text" }
                toggleIcon()
            }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }
}

internal interface IconSamples {

    fun RenderContext.icons() {
        icon("bundle".pfIcon())
        icon("clock".far())
        icon("bars".fas())
    }
}

internal interface NotificationSamples {

    fun RenderContext.add() {
        dropdown<Int> {
            textToggle { +"1, 2 or 3" }
            items {
                (1..3).forEach { item(it) }
                store.select handledBy Notification.add { item ->
                    info("You selected ${item.unwrap()}")
                }
            }
        }
    }
}

internal interface PageSamples {

    fun RenderContext.typicalSetup() {
        val router = Router(StringRoute("#home"))
        page {
            pageHeader(id = "foo") {
                brand {
                    home("#home")
                    img("/assets/logo.svg")
                }
                horizontalNavigation(router) {
                    navigationItems {
                        navigationItem("#item1", "Item 1")
                        navigationItem("#item2", "Item 2")
                    }
                }
                headerTools {
                    notificationBadge()
                }
            }
            pageSidebar {
                sidebarBody {
                    verticalNavigation(router) {
                        navigationItems {
                            navigationItem("#item1", "Item 1")
                            navigationItem("#item2", "Item 2")
                        }
                    }
                }
            }
            pageMain {
                pageSection {
                    h1 { +"Welcome" }
                    p { +"Lorem ipsum" }
                }
                pageSection {
                    +"Another section"
                }
            }
        }
    }
}

internal interface WithIdProviderSamples {

    fun RenderContext.useItemId() {
        // this ID provider will be used below
        val idProvider: IdProvider<String, String> = { Id.build(it) }

        dataList(ItemStore(idProvider)) {
            display {
                dataListItem(it) {
                    dataListRow(id = itemId(it)) {
                        +it
                    }
                }
            }
        }
    }
}