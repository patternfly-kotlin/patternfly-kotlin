package org.patternfly

import dev.fritz2.binding.QueuedUpdate
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.Events
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event

// initial data: expanded = false
class CollapseExpandStore<T>(private val root: HTMLElement) : RootStore<Boolean>(false) {

    private val closeHandler: (Event) -> Unit = {
        val clickInside = root.contains(it.target as Node)
        if (!clickInside) {
            removeCloseHandler()
            launch {
                enqueue(QueuedUpdate({ false }, ::errorHandler))
            }
        }
    }

    internal val expand: SimpleHandler<Unit> = handle { expanded ->
        val newValue = !expanded
        if (newValue) {
            document.addEventListener(Events.click.name, closeHandler)
        } else {
            removeCloseHandler()
        }
        newValue
    }

    internal val collapse: SimpleHandler<T> = handle { _, _ ->
        removeCloseHandler()
        false
    }

    val collapsed: Flow<Boolean> = data.drop(1).filter { !it } // drop initial state
    val expanded: Flow<Boolean> = data.drop(1).filter { it } // drop initial state

    private fun removeCloseHandler() {
        document.removeEventListener(Events.click.name, closeHandler)
    }
}
