package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.QueuedUpdate
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.Events
import dev.fritz2.lenses.IdProvider
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event

typealias CollapsePredicate = (Element) -> Boolean

// initial data: expanded = false
class CollapseExpandStore(private val collapsePredicate: CollapsePredicate? = null) : RootStore<Boolean>(false) {

    private var closeHandler: ((Event) -> Unit)? = null

    val collapsed: Flow<Boolean> = data.drop(1).filter { !it } // drop initial state
    val expanded: Flow<Boolean> = data.drop(1).filter { it } // drop initial state

    val expand: SimpleHandler<Unit> = handle {
        addCloseHandler()
        true
    }

    val collapse: SimpleHandler<Unit> = handle {
        removeCloseHandler()
        false
    }

    val toggle = handle { expanded ->
        if (expanded) {
            removeCloseHandler()
            false
        } else {
            addCloseHandler()
            true
        }
    }

    private fun addCloseHandler() {
        if (collapsePredicate != null) {
            closeHandler = {
                if (collapsePredicate.invoke((it.target.unsafeCast<Element>()))) {
                    removeCloseHandler()
                    launch {
                        enqueue(QueuedUpdate({ false }, ::errorHandler))
                    }
                }
            }
            document.addEventListener(Events.click.name, closeHandler)
        }
    }

    private fun removeCloseHandler() {
        closeHandler?.let {
            document.removeEventListener(Events.click.name, it)
        }
    }
}

class ItemStore<T>(val identifier: IdProvider<T, String>) :
    RootStore<Items<T>>(Items(identifier)), PageInfoHandler {

    val visible: Flow<List<T>> = data.map { it.page }
    val selected: Flow<Int> = data.map { it.selected.size }

    val addAll: Handler<List<T>> = handle { items, newItems -> items.addAll(newItems) }

    val addFilter: Handler<Pair<String, ItemFilter<T>>> = handle { items, (name, filter) ->
        items.addFilter(name, filter)
    }
    val removeFilter: Handler<String> = handle { items, name -> items.removeFilter(name) }

    override val gotoFirstPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoFirstPage()) }
    override val gotoPreviousPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoPreviousPage()) }
    override val gotoNextPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoNextPage()) }
    override val gotoLastPage: Handler<Unit> = handle { it.copy(pageInfo = it.pageInfo.gotoLastPage()) }
    override val gotoPage: Handler<Int> = handle { items, page ->
        items.copy(pageInfo = items.pageInfo.gotoPage(page))
    }
    override val pageSize: Handler<Int> = handle { items, pageSize ->
        items.copy(pageInfo = items.pageInfo.pageSize(pageSize))
    }
    override val total: Handler<Int> = handle { items, _ -> items } // not implemented!
    override val refresh: Handler<Unit> = handle { it } // not implemented!

    val selectNone: Handler<Unit> = handle { it.selectNone() }
    val selectVisible: Handler<Unit> = handle { it.selectPage() }
    val selectAll: Handler<Unit> = handle { it.selectAll() }
    val select: Handler<Pair<T, Boolean>> = handle { items, (item, select) ->
        items.select(item, select)
    }
    val toggleSelection: Handler<T> = handle { items, item -> items.toggleSelection(item) }
}
