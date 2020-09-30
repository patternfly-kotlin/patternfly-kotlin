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
    id: String? = null,
    classes: String? = null,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(store, id = id, classes = classes), content)

fun <T> DataListRow<T>.pfDataListAction(
    id: String? = null,
    classes: String? = null,
    content: DataListAction.() -> Unit = {}
): DataListAction = register(DataListAction(id = id, classes = classes), content)

fun DataListContent.pfDataListCell(
    id: String? = null,
    classes: String? = null,
    content: DataListCell.() -> Unit = {}
): DataListCell = register(DataListCell(id = id, classes = classes), content)

fun <T> DataListControl<T>.pfDataListCheck(
    id: String? = null,
    classes: String? = null,
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.itemStore, this.item, id = id, classes = classes), content)

fun <T> DataListRow<T>.pfDataListContent(
    id: String? = null,
    classes: String? = null,
    content: DataListContent.() -> Unit = {}
): DataListContent = register(DataListContent(id = id, classes = classes), content)

fun <T> DataListRow<T>.pfDataListControl(
    id: String? = null,
    classes: String? = null,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> =
    register(DataListControl(this.itemStore, this.item, this.dataListItem, id = id, classes = classes), content)

fun <T> DataListItem<T>.pfDataListExpandableContent(
    id: String? = Id.unique(ComponentType.DataList.id, "ec"),
    classes: String? = null,
    content: DataListExpandableContent<T>.() -> Unit = {}
): DataListExpandableContent<T> = register(DataListExpandableContent(this, id = id, classes = classes), content)

fun <T> DataListExpandableContent<T>.pfDataListExpandableContentBody(
    id: String? = null,
    classes: String? = null,
    content: DataListExpandableContentBody.() -> Unit = {}
): DataListExpandableContentBody =
    register(DataListExpandableContentBody(id = id, classes = classes), content)

fun <T> DataList<T>.pfDataListItem(
    item: T,
    id: String? = rowId(itemStore.identifier, item),
    classes: String? = null,
    content: DataListItem<T>.() -> Unit = {}
): DataListItem<T> = register(DataListItem(this.itemStore, item, id = id, classes = classes), content)

fun <T> DataListItem<T>.pfDataListRow(
    id: String? = null,
    classes: String? = null,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.itemStore, this.item, this, id = id, classes = classes), content)

fun <T> DataListControl<T>.pfDataListToggle(
    id: String? = Id.unique(ComponentType.DataList.id, "tgl"),
    classes: String? = null,
    content: DataListToggle<T>.() -> Unit = {}
): DataListToggle<T> =
    register(DataListToggle(this.itemStore, this.item, this.dataListItem, id = id, classes = classes), content)

// ------------------------------------------------------ tag

class DataList<T> internal constructor(internal val itemStore: ItemStore<T>, id: String?, classes: String?) :
    PatternFlyComponent<HTMLUListElement>, Ul(id = id, baseClass = classes(ComponentType.DataList, classes)) {

    lateinit var display: (T) -> DataListItem<T>

    init {
        markAs(ComponentType.DataList)
        attr("role", "list")
        itemStore.visible.each { itemStore.identifier(it) }.render { item ->
            display(item)
        }.bind()
    }
}

class DataListAction internal constructor(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("data-list".component("item-action"), classes))

class DataListCell internal constructor(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("data-list".component("cell"), classes)) {
    init {
        domNode.closest(By.classname("data-list".component("item")))?.let {
            attr("rowId", it.id)
        }
    }
}

class DataListCheck<T> internal constructor(
    private val itemStore: ItemStore<T>,
    private val item: T,
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("data-list".component("check"), classes)) {
    init {
        input {
            val inputId = Id.unique(ComponentType.DataList.id, "chk")
            name = const(inputId)
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

class DataListContent internal constructor(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("data-list".component("item-content"), classes)) {
    init {
        domNode.closest(By.classname("data-list".component("item")))?.let {
            attr("rowId", it.id)
        }
    }
}

class DataListControl<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    internal val dataListItem: DataListItem<T>,
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("data-list".component("item-control"), classes))

class DataListExpandableContent<T> internal constructor(
    dataListItem: DataListItem<T>,
    id: String?,
    classes: String?
) : TextElement(
    "section",
    id = id,
    baseClass = classes("data-list".component("expandable-content"), classes)
) {
    init {
        domNode.hidden = true // tp prevent flickering during updates
        if (dataListItem.toggleButton != null && id != null) {
            dataListItem.toggleButton!!.aria["controls"] = id
        }
        dataListItem.expanded.data.map { !it }.bindAttr("hidden")
    }
}

class DataListExpandableContentBody internal constructor(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("data-list".component("expandable-content", "body"), classes))

class DataListItem<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    id: String?,
    classes: String?
) : Li(id = id, baseClass = classes("data-list".component("item"), classes)) {

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
    id: String?,
    classes: String?
) : Div(id = id, baseClass = classes("data-list".component("item-row"), classes)) {
    init {
        domNode.closest(By.classname("data-list".component("item")))?.let {
            attr("rowId", it.id)
        }
    }
}

class DataListToggle<T> internal constructor(
    private val itemStore: ItemStore<T>,
    private val item: T,
    private val dataListItem: DataListItem<T>,
    id: String?,
    classes: String?
) : Div(baseClass = classes("data-list".component("toggle"), classes)) {
    init {
        pfButton(id = id, classes = "plain".modifier()) {
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
