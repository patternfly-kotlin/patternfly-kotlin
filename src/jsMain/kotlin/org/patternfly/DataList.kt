package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.const
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.map
import org.patternfly.Modifier.selectable

typealias DataListDisplay<T> = (T) -> DataListItem<T>.() -> Unit

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDataList(
    identifier: IdProvider<T, String>,
    store: DataListStore<T>,
    selectionMode: SelectionMode = SelectionMode.NONE,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(identifier, store, selectionMode), content)

fun <T> DataListRow<T>.pfDataListAction(
    vararg classes: String,
    content: DataListAction<T>.() -> Unit = {}
): DataListAction<T> = register(DataListAction(this.dataList, this.item, classes.toList()), content)

fun <T> DataListContent<T>.pfDataListCell(
    vararg classes: String,
    content: DataListCell<T>.() -> Unit = {}
): DataListCell<T> = register(DataListCell(this.dataList, this.item, classes.toList()), content)

fun <T> DataListControl<T>.pfDataListCheck(
    checkBoxName: String = Id.unique("dl-checkbox"),
    content: DataListCheck<T>.() -> Unit = {}
): DataListCheck<T> = register(DataListCheck(this.dataList, this.item, checkBoxName), content)

fun <T> DataListRow<T>.pfDataListContent(
    vararg classes: String,
    content: DataListContent<T>.() -> Unit = {}
): DataListContent<T> = register(DataListContent(this.dataList, this.item, classes.toList()), content)

fun <T> DataListRow<T>.pfDataListControl(
    vararg classes: String,
    content: DataListControl<T>.() -> Unit = {}
): DataListControl<T> = register(DataListControl(this.dataList, this.item, classes.toList()), content)

fun <T> DataListItem<T>.pfDataListRow(
    vararg classes: String,
    content: DataListRow<T>.() -> Unit = {}
): DataListRow<T> = register(DataListRow(this.dataList, this.item, classes.toList()), content)

// ------------------------------------------------------ tag

class DataList<T> internal constructor(
    internal val identifier: IdProvider<T, String>,
    internal val store: DataListStore<T>,
    internal val selectionMode: SelectionMode
) : Ul(baseClass = "data-list".component()) {

    var asText: AsText<T> = { it.toString() }
    var display: DataListDisplay<T> = {
        {
            +this@DataList.asText.invoke(it)
        }
    }

    init {
        domNode.componentType(ComponentType.DataList)
        attr("role", "list")
        store.data.each().map { item ->
            render {
                register(DataListItem(this@DataList, item)) {}
            }
        }.bind()
    }
}

class DataListAction<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: List<String>
) : Div(baseClass = buildString {
    append("data-list".component("item-action"))
    if (classes.isNotEmpty()) {
        classes.joinTo(this, " ", " ")
    }
})

class DataListCell<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: List<String>
) : Div(baseClass = buildString {
    append("data-list".component("cell"))
    if (classes.isNotEmpty()) {
        classes.joinTo(this, " ", " ")
    }
}) {
    init {
        attr("rowId", rowId(dataList.identifier, item))
    }
}

class DataListCheck<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    checkBoxName: String
) : Div(baseClass = "data-list".component("check")) {
    init {
        input {
            name = const(checkBoxName)
            type = const("checkbox")
            aria["invalid"] = false
            aria["labelledby"] = this@DataListCheck.dataList.identifier(this@DataListCheck.item)
            selects.map { this@DataListCheck.item } handledBy this@DataListCheck.dataList.store.selection
        }
    }
}

class DataListContent<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: List<String>
) : Div(baseClass = buildString {
    append("data-list".component("item-content"))
    if (classes.isNotEmpty()) {
        classes.joinTo(this, " ", " ")
    }
}) {
    init {
        attr("rowId", rowId(dataList.identifier, item))
    }
}

class DataListControl<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: List<String>
) : Div(baseClass = buildString {
    append("data-list".component("item-control"))
    if (classes.isNotEmpty()) {
        classes.joinTo(this, " ", " ")
    }
})

class DataListItem<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T
) : Li(id = rowId<T>(dataList.identifier, item), baseClass = "data-list".component("item")) {
    init {
        aria["labelledby"] = dataList.identifier(item)
        if (dataList.selectionMode != SelectionMode.NONE) {
            attr("tabindex", "0")
            domNode.classList += selectable
            clicks.map { item } handledBy dataList.store.selection
        }
        val content = dataList.display.invoke(item)
        content.invoke(this)
    }
}

class DataListRow<T> internal constructor(
    internal val dataList: DataList<T>,
    internal val item: T,
    classes: List<String>
) : Div(baseClass = buildString {
    append("data-list".component("item-row"))
    if (classes.isNotEmpty()) {
        classes.joinTo(this, " ", " ")
    }
}) {
    init {
        attr("rowId", rowId(dataList.identifier, item))
    }
}

// ------------------------------------------------------ store

open class DataListStore<T>(private val identifier: IdProvider<T, String>) : RootStore<List<T>>(listOf()) {
    val selection = handleAndOffer<T, T> { items, item ->
        offer(item)
        items
    }

    val remove = handle<String> { items, id ->
        items.filterNot { identifier(it) == id }
    }

    val empty = data.map { it.isEmpty() }
}

// ------------------------------------------------------ internals

private fun <T> rowId(identifier: IdProvider<T, String>, item: T): String = "${identifier(item)}-row"
