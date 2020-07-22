package org.patternfly

import dev.fritz2.binding.RootStore
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

// ------------------------------------------------------ dsl

typealias DataListDisplay<T> = (T) -> Li.() -> Unit

fun <T> HtmlElements.pfDataList(
    identifier: IdProvider<T, String>,
    selectionMode: SelectionMode = SelectionMode.NONE,
    store: DataListStore<T>,
    content: DataList<T>.() -> Unit = {}
): DataList<T> = register(DataList(identifier, selectionMode, store), content)

fun HtmlElements.pfDataListItemRow(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "data-list".component("item-row")), content)

fun HtmlElements.pfDataListItemControl(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "data-list".component("item-control")), content)

fun HtmlElements.pfDataListItemContent(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "data-list".component("item-content")), content)

fun HtmlElements.pfDataListItemCell(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "data-list".component("cell")), content)

fun HtmlElements.pfDataListItemAction(content: Div.() -> Unit = {}): Div =
    register(Div(baseClass = "data-list".component("item-action")), content)

// ------------------------------------------------------ tag

class DataList<T> internal constructor(
    private val identifier: IdProvider<T, String>,
    private val selectionMode: SelectionMode,
    private val store: DataListStore<T>
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
                li(baseClass = "data-list".component("item")) {
                    attr("tabindex", "0")
                    if (this@DataList.selectionMode != SelectionMode.NONE) {
                        domNode.classList += selectable
                        clicks.map { item } handledBy this@DataList.store.selection
                    }
                    val content = this@DataList.display.invoke(item)
                    content.invoke(this)
                }
            }
        }.bind()
    }
}

// ------------------------------------------------------ store

class DataListStore<T>(private val identifier: IdProvider<T, String>) : RootStore<List<T>>(listOf()) {
    val selection = handleAndOffer<T, T> { items, item ->
        offer(item)
        items
    }

    val remove = handle<String> { items, id ->
        items.filterNot { identifier(it) == id }
    }

    val empty = data.map { it.isEmpty() }
}
