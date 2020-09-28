package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.states
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDataList(
    store: ItemStore<T>,
    classes: String? = null,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(store, classes), content)

fun <T> DataListRow<T>.pfDataListAction(
    classes: String? = null,
    content: DataListAction.() -> Unit = {}
): DataListAction = register(DataListAction(classes), content)

fun <T> DataListContent<T>.pfDataListCell(
    classes: String? = null,
    content: DataListCell<T>.() -> Unit = {}
): DataListCell<T> = register(DataListCell(this.itemStore, this.item, classes), content)

fun <T> DataListControl<T>.pfDataListCheck(
    classes: String? = null,
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.itemStore, this.item, classes), content)

fun <T> DataListRow<T>.pfDataListContent(
    classes: String? = null,
    content: DataListContent<T>.() -> Unit = {}
): DataListContent<T> = register(DataListContent(this.itemStore, this.item, classes), content)

fun <T> DataListRow<T>.pfDataListControl(
    classes: String? = null,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> = register(DataListControl(this.itemStore, this.item, this.dataListItem, classes), content)

fun <T> DataListItem<T>.pfDataListExpandableContent(
    classes: String? = null,
    content: DataListExpandableContent<T>.() -> Unit = {}
): DataListExpandableContent<T> = register(DataListExpandableContent(this, classes), content)

fun <T> DataListExpandableContent<T>.pfDataListExpandableContentBody(
    classes: String? = null,
    content: DataListExpandableContentBody<T>.() -> Unit = {}
): DataListExpandableContentBody<T> =
    register(DataListExpandableContentBody(classes), content)

fun <T> DataList<T>.pfDataListItem(
    item: T,
    classes: String? = null,
    content: DataListItem<T>.() -> Unit = {}
): DataListItem<T> = register(DataListItem(this.itemStore, item, classes), content)

fun <T> DataListItem<T>.pfDataListRow(
    classes: String? = null,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.itemStore, this.item, this, classes), content)

fun <T> DataListControl<T>.pfDataListToggle(
    classes: String? = null,
    content: DataListToggle<T>.() -> Unit = {}
): DataListToggle<T> = register(DataListToggle(this.itemStore, this.item, this.dataListItem, classes), content)

// ------------------------------------------------------ tag

class DataList<T> internal constructor(internal val itemStore: ItemStore<T>, classes: String?) :
    PatternFlyComponent<HTMLUListElement>, Ul(baseClass = classes(ComponentType.DataList, classes)) {

    lateinit var display: (T) -> DataListItem<T>

    init {
        markAs(ComponentType.DataList)
        attr("role", "list")
        itemStore.visible.each { itemStore.identifier(it) }.render { item ->
            display(item)
        }.bind()
    }
}

class DataListAction internal constructor(classes: String?) :
    Div(baseClass = classes("data-list".component("item-action"), classes))

class DataListCell<T> internal constructor(itemStore: ItemStore<T>, item: T, classes: String?) :
    Div(baseClass = classes("data-list".component("cell"), classes)) {
    init {
        attr("rowId", rowId(itemStore.identifier, item))
    }
}

class DataListCheck<T> internal constructor(
    private val itemStore: ItemStore<T>,
    private val item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("check"), classes)) {
    init {
        input {
            val id = Id.unique(ComponentType.DataList.id, "chk")
            name = const(id)
            domNode.type = "checkbox"
//            type = const("checkbox") // this causes visual flickering
            aria["invalid"] = false
            aria["labelledby"] = this@DataListCheck.itemStore.identifier(this@DataListCheck.item)
            changes.states()
                .map { (this@DataListCheck.item to it) }
                .handledBy(this@DataListCheck.itemStore.select)
            checked = this@DataListCheck.itemStore.data.map { it.isSelected(this@DataListCheck.item) }
        }
    }
}

class DataListContent<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-content"), classes)) {
    init {
        attr("rowId", rowId(itemStore.identifier, item))
    }
}

class DataListControl<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-control"), classes))

class DataListExpandableContent<T> internal constructor(dataListItem: DataListItem<T>, classes: String?) :
    TextElement("section", baseClass = classes("data-list".component("expandable-content"), classes)) {
    init {
        val id = Id.unique(ComponentType.DataList.id, "ec")
        domNode.id = id
        domNode.hidden = true // tp prevent flickering during updates
        dataListItem.toggleButton?.let {
            it.aria["controls"] = id
        }
        dataListItem.expanded.data.map { !it }.bindAttr("hidden")
    }
}

class DataListExpandableContentBody<T> internal constructor(classes: String?) :
    Div(baseClass = classes("data-list".component("expandable-content", "body"), classes))

class DataListItem<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    classes: String?
) : Li(id = rowId<T>(itemStore.identifier, item), baseClass = classes("data-list".component("item"), classes)) {

    val expanded: CollapseExpandStore = CollapseExpandStore()
    internal var toggleButton: HTMLButtonElement? = null

    init {
        classMap = expanded.data.map { mapOf("expanded".modifier() to it) }
        aria["labelledby"] = itemStore.identifier(item)
    }
}

class DataListRow<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-row"), classes)) {
    init {
        attr("rowId", rowId(itemStore.identifier, item))
    }
}

class DataListToggle<T> internal constructor(
    private val itemStore: ItemStore<T>,
    private val item: T,
    private val dataListItem: DataListItem<T>,
    classes: String?
) : Div(baseClass = classes("data-list".component("toggle"), classes)) {
    init {
        val id = Id.unique(ComponentType.DataList.id, "tgl")
        pfButton("plain".modifier()) {
            domNode.id = id
            this@DataListToggle.dataListItem.toggleButton = domNode
            aria["labelledby"] = "$id ${this@DataListToggle.itemStore.identifier(this@DataListToggle.item)}"
            aria["label"] = "Details"
            div(baseClass = "data-list".component("toggle", "icon")) {
                pfIcon("angle-right".fas())
            }
            clicks handledBy this@DataListToggle.dataListItem.expanded.toggle
            this@DataListToggle.dataListItem.expanded.data.map { it.toString() }.bindAttr("aria-expanded")
        }
    }
}

// ------------------------------------------------------ internals

private fun <T> rowId(identifier: IdProvider<T, String>, item: T): String = "${identifier(item)}-row"
