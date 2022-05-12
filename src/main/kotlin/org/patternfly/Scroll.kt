package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.WindowListener
import dev.fritz2.dom.html.Events
import kotlinx.browser.document
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.patternfly.dom.isInView
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event

internal fun Element.updateScrollButtons(): ScrollButton? {
    val left = this.firstElementChild
    val right = this.lastElementChild
    return if (left != null && right != null) {
        val overflowOnLeft = !left.isInView(this)
        val overflowOnRight = !right.isInView(this)
        val showButtons = overflowOnLeft || overflowOnRight
        val disableLeft = !overflowOnLeft
        val disableRight = !overflowOnRight
        ScrollButton(showButtons, disableLeft, disableRight)
    } else null
}

// find first element that is fully in view on the left, then scroll to the element before it
internal fun Element.scrollLeft() {
    var firstElementInView: HTMLElement? = null
    var lastElementOutOfView: HTMLElement? = null
    val iterator = this.childNodes.asList().filterIsInstance<HTMLElement>().listIterator()

    while (iterator.hasNext() && firstElementInView == null) {
        val child = iterator.next()
        if (child.isInView(this)) {
            firstElementInView = child
            if (iterator.hasPrevious()) {
                lastElementOutOfView = iterator.previous()
            }
        }
    }
    if (lastElementOutOfView != null) {
        this.scrollLeft -= lastElementOutOfView.scrollWidth
    }
}

// find last Element that is fully in view on the right, then scroll to the element after it
internal fun Element.scrollRight() {
    var lastElementInView: HTMLElement? = null
    var firstElementOutOfView: HTMLElement? = null
    val elements = this.childNodes.asList().filterIsInstance<HTMLElement>()
    val iterator = elements.listIterator(elements.size)

    while (iterator.hasPrevious() && lastElementInView == null) {
        val child = iterator.previous()
        if (child.isInView(this)) {
            lastElementInView = child
            if (iterator.hasNext()) {
                firstElementOutOfView = iterator.next()
            }
        }
    }
    if (firstElementOutOfView != null) {
        this.scrollLeft += firstElementOutOfView.scrollWidth
    }
}

internal data class ScrollButton(
    val showButtons: Boolean = false,
    val disableLeft: Boolean = true,
    val disableRight: Boolean = false
) {
    companion object {
        // Using scrolls.map leads to a CCE :-(
        internal fun scrolls(element: Element): Flow<Event> = callbackFlow {
            val listener: (Event) -> Unit = { this.trySend(it).isSuccess }
            element.addEventListener(Events.scroll.name, listener)
            awaitClose { element.removeEventListener(Events.scroll.name, listener) }
        }

        // Window.resizes gives an error, so we implement it ourselves using document.defaultView
        internal fun windowResizes(): WindowListener<Event> = WindowListener(
            callbackFlow {
                val listener: (Event) -> Unit = { this.trySend(it).isSuccess }
                document.defaultView?.addEventListener(Events.resize.name, listener)
                awaitClose { document.defaultView?.removeEventListener(Events.resize.name, listener) }
            }
        )
    }
}

internal class ScrollButtonStore : RootStore<ScrollButton>(ScrollButton())
