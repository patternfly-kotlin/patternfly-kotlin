package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.states
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.map
import org.patternfly.Modifier.selectable
import org.w3c.dom.HTMLUListElement

typealias DataListDisplay<T> = (T) -> DataListItem<T>.() -> Unit

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDataList(
    identifier: IdProvider<T, String>,
    store: ItemStore<T>,
    selectionMode: SelectionMode = SelectionMode.NONE,
    classes: String? = null,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(identifier, store, selectionMode, classes), content)

fun <T> HtmlElements.pfDataList(
    identifier: IdProvider<T, String>,
    store: ItemStore<T>,
    selectionMode: SelectionMode = SelectionMode.NONE,
    modifier: Modifier,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(identifier, store, selectionMode, modifier.value), content)

fun <T> DataListRow<T>.pfDataListAction(
    classes: String? = null,
    content: DataListAction<T>.() -> Unit = {}
): DataListAction<T> = register(DataListAction(this.dataList, this.item, classes), content)

fun <T> DataListRow<T>.pfDataListAction(
    modifier: Modifier,
    content: DataListAction<T>.() -> Unit = {}
): DataListAction<T> = register(DataListAction(this.dataList, this.item, modifier.value), content)

fun <T> DataListContent<T>.pfDataListCell(
    classes: String? = null,
    content: DataListCell<T>.() -> Unit = {}
): DataListCell<T> = register(DataListCell(this.dataList, this.item, classes), content)

fun <T> DataListContent<T>.pfDataListCell(
    modifier: Modifier,
    content: DataListCell<T>.() -> Unit = {}
): DataListCell<T> = register(DataListCell(this.dataList, this.item, modifier.value), content)

fun <T> DataListControl<T>.pfDataListCheck(
    checkBoxName: String = Id.unique("dl-checkbox"),
    classes: String? = null,
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.dataList, this.item, checkBoxName, classes), content)

fun <T> DataListControl<T>.pfDataListCheck(
    checkBoxName: String = Id.unique("dl-checkbox"),
    modifier: Modifier,
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.dataList, this.item, checkBoxName, modifier.value), content)

fun <T> DataListRow<T>.pfDataListContent(
    classes: String? = null,
    content: DataListContent<T>.() -> Unit = {}
): DataListContent<T> = register(DataListContent(this.dataList, this.item, classes), content)

fun <T> DataListRow<T>.pfDataListContent(
    modifier: Modifier,
    content: DataListContent<T>.() -> Unit = {}
): DataListContent<T> = register(DataListContent(this.dataList, this.item, modifier.value), content)

fun <T> DataListRow<T>.pfDataListControl(
    classes: String? = null,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> = register(DataListControl(this.dataList, this.item, classes), content)

fun <T> DataListRow<T>.pfDataListControl(
    modifier: Modifier,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> = register(DataListControl(this.dataList, this.item, modifier.value), content)

fun <T> DataListItem<T>.pfDataListRow(
    classes: String? = null,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.dataList, this.item, classes), content)

fun <T> DataListItem<T>.pfDataListRow(
    modifier: Modifier,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.dataList, this.item, modifier.value), content)

// ------------------------------------------------------ tag

class DataList<T> internal constructor(
    internal val identifier: IdProvider<T, String>,
    internal val store: ItemStore<T>,
    internal val selectionMode: SelectionMode,
    classes: String?
) : PatternFlyComponent<HTMLUListElement>, Ul(baseClass = classes(ComponentType.DataList, classes)) {

    var asText: AsText<T> = { it.toString() }
    var display: DataListDisplay<T> = {
        {
            +this@DataList.asText.invoke(it)
        }
    }

    init {
        markAs(ComponentType.DataList)
        attr("role", "list")
        store.visibleItems.each().render { item ->
            register(DataListItem(this@DataList, item)) {}
        }.bind()
    }
}

class DataListAction<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-action"), classes))

class DataListCell<T> internal constructor(
    dataList: DataList<T>,
    item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("cell"), classes)) {
    init {
        attr("rowId", rowId(dataList.identifier, item))
    }
}

class DataListCheck<T> internal constructor(
    private val dataList: DataList<T>,
    private val item: T,
    checkBoxName: String,
    classes: String?
) : Div(baseClass = classes("data-list".component("check"), classes)) {
    init {
        input {
            name = const(checkBoxName)
            type = const("checkbox")
            aria["invalid"] = false
            aria["labelledby"] = this@DataListCheck.dataList.identifier(this@DataListCheck.item)
            changes.states()
                .map { (this@DataListCheck.item to it) }
                .handledBy(this@DataListCheck.dataList.store.select)
        }
    }
}

class DataListContent<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-content"), classes)) {
    init {
        attr("rowId", rowId(dataList.identifier, item))
    }
}

class DataListControl<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-control"), classes))

class DataListItem<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T
) : Li(id = rowId<T>(dataList.identifier, item), baseClass = "data-list".component("item")) {
    init {
        aria["labelledby"] = dataList.identifier(item)
        if (dataList.selectionMode != SelectionMode.NONE) {
            attr("tabindex", "0")
            domNode.classList += selectable
            clicks.map { item } handledBy dataList.store.toggleSelection
        }
        val content = dataList.display.invoke(item)
        content.invoke(this)
    }
}

class DataListRow<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: String?
) : Div(baseClass = classes("data-list".component("item-row"), classes)) {
    init {
        attr("rowId", rowId(dataList.identifier, item))
    }
}

// ------------------------------------------------------ internals

private fun <T> rowId(identifier: IdProvider<T, String>, item: T): String = "${identifier(item)}-row"
