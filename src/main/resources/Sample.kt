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
import kotlin.random.Random

public object AlertSamples {

    public fun RenderContext.alert() {
        alert(Severity.INFO, "Alert title") {
            alertDescription { +"Lorem ipsum dolor sit amet." }
            alertActions {
                pushButton(inline, link) { +"View details" }
                pushButton(inline, link) { +"Ignore" }
            }
        }
    }

    public fun RenderContext.alertGroup() {
        alertGroup {
            alert(Severity.INFO, "Just saying.", inline = true)
            alert(Severity.SUCCESS, "Well done!", inline = true)
            alert(Severity.WARNING, "Really?", inline = true)
            alert(Severity.DANGER, "You're in trouble!", inline = true)
        }
    }

    public fun RenderContext.description() {
        alert(Severity.INFO, "Alert title") {
            alertDescription { +"Lorem ipsum dolor sit amet." }
        }
    }

    public fun RenderContext.actions() {
        alert(Severity.INFO, "Alert title") {
            alertActions {
                pushButton(inline, link) { +"View details" }
                pushButton(inline, link) { +"Ignore" }
            }
        }
    }

    public fun RenderContext.closes() {
        alert(Severity.INFO, "Close me", closable = true) {
            closes handledBy Notification.info("You did it!")
        }
    }
}

public object BadgeSamples {

    public fun RenderContext.badge() {
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

public object ButtonSamples {

    public fun RenderContext.pushButton() {
        pushButton { +"Button" }
    }

    public fun RenderContext.linkButton() {
        linkButton {
            +"PatternFly"
            href("https://patternfly.org")
        }
    }

    public fun Div.clickButton() {
        clickButton(primary) {
            +"Click me"
        } handledBy Notification.info("Score!")
    }

    public fun RenderContext.buttonIcon() {
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

    public fun RenderContext.justIcon() {
        pushButton(plain) {
            icon("user".fas())
        }
    }
}

public object CardSamples {

    public fun RenderContext.card() {
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

    public fun RenderContext.cardHeaderMain() {
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

    public fun RenderContext.cardTitleInHeader() {
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

    public fun RenderContext.cardTitleInCard() {
        card {
            cardTitle { +"Title" }
            cardBody { +"Body" }
            cardFooter { +"Footer" }
        }
    }

    public fun RenderContext.multipleBodies() {
        card {
            cardTitle { +"Title" }
            cardBody { +"Body" }
            cardBody { +"Body" }
            cardBody { +"Body" }
            cardFooter { +"Footer" }
        }
    }
}

public object CardViewSamples {

    public fun RenderContext.cardView() {
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

public object ChipSamples {

    public fun RenderContext.chip() {
        chip { +"Chip" }
        chip(readOnly = true) { +"Read-only chip" }
        chip {
            +"With badge"
            badge { value(42) }
        }
    }

    public fun RenderContext.closes() {
        chip {
            +"Close me"
            closes handledBy Notification.info("You did it!")
        }
    }
}

public object ChipGroupSamples {

    public fun RenderContext.vararg() {
        chipGroup<String> {
            +"Vararg demo"
            chips("Foo", "Bar")
        }
    }

    public fun RenderContext.list() {
        chipGroup<String> {
            +"List demo"
            chips(listOf("Foo", "Bar"))
        }
    }

    public fun RenderContext.builder() {
        chipGroup<String> {
            +"Builder demo"
            chips {
                +"Foo"
                add("Bar")
            }
        }
    }

    public fun RenderContext.display() {
        chipGroup<String> {
            +"Display demo"
            display {
                chip { +it.toUpperCase() }
            }
            chips("Foo", "Bar")
        }
    }

    public fun RenderContext.store() {
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

    public fun RenderContext.closes() {
        chipGroup<String>(closable = true) {
            +"Close me"
            chips("Foo", "Bar")
            closes handledBy Notification.info("You did it!")
        }
    }

    public fun RenderContext.remove() {
        chipGroup<String> {
            +"Remove one"
            chips("Foo", "Bar")
            store.remove handledBy Notification.add { chip ->
                info("You removed $chip.")
            }
        }
    }
}

public object CSSSamples {

    public fun component() {
        "card".component() // pf-c-card
        "card".component("header") // pf-c-card__header
        "card".component("header", "main") // pf-c-card__header-main
    }

    public fun classesDsl() {
        val disabled = Random.nextBoolean()
        val classes = classes {
            +"button".component()
            +"plain".modifier()
            +("disabled".modifier() `when` disabled)
        }
    }

    public fun classesVararg() {
        val classes = classes(
            "button".component(),
            "plain".modifier(),
            "disabled".modifier()
        )
    }
}

public object DataListSamples {

    public fun RenderContext.dataList() {
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

    public fun RenderContext.ces() {
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

    public fun RenderContext.selects() {
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

public object DataTableSamples {

    public fun RenderContext.dataTable() {
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

    public fun RenderContext.dataColumns() {
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

    public fun RenderContext.selects() {
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

public object DrawerSamples {

    public fun RenderContext.drawerSetup() {
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

    public fun RenderContext.ces() {
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

    public fun RenderContext.drawerContents() {
        drawer {
            drawerContent {
                drawerBody { +"Actual" }
                drawerBody { +"content" }
                drawerBody { +"goes here" }
            }
        }
    }

    public fun RenderContext.drawerPanels() {
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

public object DropdownSamples {

    public fun RenderContext.dropdownDsl() {
        dropdown<String> {
            textToggle { +"Choose one" }
            groups {
                group { // w/o title
                    item("Item 1")
                    item("Item 2") {
                        description = "Item description"
                    }
                }
                separator()
                group("Group 1") {
                    item("Item 1")
                    item("Item 2") {
                        disabled = true
                    }
                }
                separator()
                group("Group 2") {
                    item("Item 1")
                    item("Item 2")
                }
            }
        }
    }

    public fun RenderContext.dropdownStore() {
        data class Demo(val id: String, val name: String)

        val store = DropdownStore<Demo>().apply {
            select handledBy Notification.add { demo ->
                info("You selected ${demo.name}")
            }
        }
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

    public fun RenderContext.ces() {
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

    public fun RenderContext.textToggle() {
        dropdown<String> {
            textToggle { +"Text" }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    public fun RenderContext.iconToggle() {
        dropdown<String> {
            iconToggle { icon("user".fas()) }
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    public fun RenderContext.kebabToggle() {
        dropdown<String> {
            kebabToggle()
            items {
                item("Foo")
                item("Bar")
            }
        }
    }

    public fun RenderContext.checkboxToggle() {
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

    public fun RenderContext.actionToggle() {
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

    public fun RenderContext.customToggle() {
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

public object IconSamples {

    public fun RenderContext.icons() {
        icon("bundle".pfIcon())
        icon("clock".far())
        icon("bars".fas())
    }
}

public object NotificationSamples {

    public fun RenderContext.add() {
        dropdown<Int> {
            textToggle { +"1, 2 or 3" }
            items {
                (1..3).forEach { item(it) }
                store.select handledBy Notification.add { item ->
                    info("You selected $item")
                }
            }
        }
    }
}

public object PageSamples {

    public fun RenderContext.typicalSetup() {
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

public object WithIdProviderSamples {

    public fun RenderContext.useItemId() {
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