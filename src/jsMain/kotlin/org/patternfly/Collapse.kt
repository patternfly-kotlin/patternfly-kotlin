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