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
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDataList(
    store: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(store, id = id, baseClass = baseClass), content)

fun <T> DataListRow<T>.pfDataListAction(
    id: String? = null,
    baseClass: String? = null,
    content: DataListAction.() -> Unit = {}
): DataListAction = register(DataListAction(id = id, baseClass = baseClass), content)

fun DataListContent.pfDataListCell(
    id: String? = null,
    baseClass: String? = null,
    content: DataListCell.() -> Unit = {}
): DataListCell = register(DataListCell(id = id, baseClass = baseClass), content)

fun <T> DataListControl<T>.pfDataListCheck(
    id: String? = null,
    baseClass: String? = null,
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.itemStore, this.item, id = id, baseClass = baseClass), content)

fun <T> DataListRow<T>.pfDataListContent(
    id: String? = null,
    baseClass: String? = null,
    content: DataListContent.() -> Unit = {}
): DataListContent = register(DataListContent(id = id, baseClass = baseClass), content)

fun <T> DataListRow<T>.pfDataListControl(
    id: String? = null,
    baseClass: String? = null,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> =
    register(DataListControl(this.itemStore, this.item, this.dataListItem, id = id, baseClass = baseClass), content)

fun <T> DataListItem<T>.pfDataListExpandableContent(
    id: String? = Id.unique(ComponentType.DataList.id, "ec"),
    baseClass: String? = null,
    content: DataListExpandableContent<T>.() -> Unit = {}
): DataListExpandableContent<T> = register(DataListExpandableContent(this, id = id, baseClass = baseClass), content)

fun <T> DataListExpandableContent<T>.pfDataListExpandableContentBody(
    id: String? = null,
    baseClass: String? = null,
    content: DataListExpandableContentBody.() -> Unit = {}
): DataListExpandableContentBody =
    register(DataListExpandableContentBody(id = id, baseClass = baseClass), content)

fun <T> DataList<T>.pfDataListItem(
    item: T,
    id: String? = "${itemStore.identifier(item)}-row",
    baseClass: String? = null,
    content: DataListItem<T>.() -> Unit = {}
): DataListItem<T> = register(DataListItem(this.itemStore, item, id = id, baseClass = baseClass), content)

fun <T> DataListItem<T>.pfDataListRow(
    id: String? = null,
    baseClass: String? = null,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.itemStore, this.item, this, id = id, baseClass = baseClass), content)

fun <T> DataListControl<T>.pfDataListToggle(
    id: String? = Id.unique(ComponentType.DataList.id, "tgl"),
    baseClass: String? = null,
    content: DataListToggle<T>.() -> Unit = {}
): DataListToggle<T> =
    register(DataListToggle(this.itemStore, this.item, this.dataListItem, id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

class DataList<T> internal constructor(internal val itemStore: ItemStore<T>, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLUListElement>, Ul(id = id, baseClass = classes(ComponentType.DataList, baseClass)) {

    lateinit var display: (T) -> DataListItem<T>

    init {
        markAs(ComponentType.DataList)
        attr("role", "list")
        itemStore.visible.each { itemStore.identifier(it) }.render { item ->
            display(item)
        }.bind()
    }
}

class DataListAction internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("data-list".component("item-action"), baseClass))

class DataListCell internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("data-list".component("cell"), baseClass)) {
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
    baseClass: String?
) : Div(id = id, baseClass = classes("data-list".component("check"), baseClass)) {
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

class DataListContent internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("data-list".component("item-content"), baseClass)) {
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
    baseClass: String?
) : Div(id = id, baseClass = classes("data-list".component("item-control"), baseClass))

class DataListExpandableContent<T> internal constructor(
    dataListItem: DataListItem<T>,
    id: String?,
    baseClass: String?
) : TextElement(
    "section",
    id = id,
    baseClass = classes("data-list".component("expandable-content"), baseClass)
) {
    init {
        domNode.hidden = true // tp prevent flickering during updates
        if (dataListItem.toggleButton != null && id != null) {
            dataListItem.toggleButton!!.aria["controls"] = id
        }
        dataListItem.expanded.data.map { !it }.bindAttr("hidden")
    }
}

class DataListExpandableContentBody internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("data-list".component("expandable-content", "body"), baseClass))

class DataListItem<T> internal constructor(
    internal val itemStore: ItemStore<T>,
    internal val item: T,
    id: String?,
    baseClass: String?
) : Li(id = id, baseClass = classes("data-list".component("item"), baseClass)) {

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
    baseClass: String?
) : Div(id = id, baseClass = classes("data-list".component("item-row"), baseClass)) {
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
    baseClass: String?
) : Div(baseClass = classes("data-list".component("toggle"), baseClass)) {
    init {
        pfButton(id = id, baseClass = "plain".modifier()) {
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
