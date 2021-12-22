package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.QueuedUpdate
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Events
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event

internal typealias CollapsePredicate = (Element) -> Boolean

/**
 * Store to manage the collapsed and expanded state of various components like [Dropdown], [Drawer] or [OptionsMenu].
 *
 * A state of `false` means collapsed and `true` means expanded. The initial state is `false` (collapsed).
 */
public class ExpandedStore internal constructor(private val collapsePredicate: CollapsePredicate? = null) :
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
     * Sets the current state to collapsed.
     */
    public val collapse: Handler<Unit> = handle {
        removeCloseHandler()
        false
    }

    /**
     * Sets the current state to expanded.
     */
    public val expand: Handler<Unit> = handle {
        addCloseHandler()
        true
    }

    /**
     * Toggles the current state.
     */
    public val toggle: Handler<Unit> = handle { expanded ->
        if (expanded) {
            removeCloseHandler()
            false
        } else {
            addCloseHandler()
            true
        }
    }

    public fun <E : Element> Tag<E>.hideIfCollapsed() {
        this.attr("hidden", data.map { expanded -> !expanded })
    }

    public fun <E : Element> Tag<E>.toggleAriaExpanded() {
        this.attr("aria-expanded", data.map { expanded -> expanded.toString() })
    }

    public fun <E : Element> Tag<E>.toggleDisplayNone() {
        this.classMap(data.map { expanded -> mapOf("display-none".util() to !expanded) })
    }

    public fun <E : Element> Tag<E>.toggleExpanded() {
        this.classMap(data.map { expanded -> mapOf("expanded".modifier() to expanded) })
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
