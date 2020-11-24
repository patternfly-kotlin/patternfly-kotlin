package org.patternfly

import dev.fritz2.binding.QueuedUpdate
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.Events
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * Predicate used by the [CollapseExpandStore] to decide whether to register a handler which calls the [CollapseExpandStore.collapse] handler if the predicate resolves to `true`.
 *
 * This predicate is used for example to collapse a dropdown if the user clicks outside of the dropdown.
 */
public typealias CollapsePredicate = (Element) -> Boolean

/**
 * Store to manage the expanded state of various components like [Dropdown], [Drawer] or [OptionsMenu]. The initial state is `false` which means collapsed (not expanded).
 */
public class CollapseExpandStore(private val collapsePredicate: CollapsePredicate? = null) :
    RootStore<Boolean>(false) {

    private var closeHandler: ((Event) -> Unit)? = null

    /**
     * Whether the current state is collapsed.
     */
    public val collapsed: Flow<Boolean> = data.drop(1).filter { !it } // drop initial state

    /**
     * Whether the current state is expanded.
     */
    public val expanded: Flow<Boolean> = data.drop(1).filter { it } // drop initial state

    /**
     * Sets the current state to expanded.
     */
    public val expand: SimpleHandler<Unit> = handle {
        addCloseHandler()
        true
    }

    /**
     * Sets the current state to collapsed.
     */
    public val collapse: SimpleHandler<Unit> = handle {
        removeCloseHandler()
        false
    }

    /**
     * Toggles the current state.
     */
    public val toggle: SimpleHandler<Unit> = handle { expanded ->
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
                    MainScope().launch {
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