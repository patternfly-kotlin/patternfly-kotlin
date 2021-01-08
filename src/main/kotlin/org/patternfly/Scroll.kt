package org.patternfly

import dev.fritz2.binding.RootStore
import org.patternfly.dom.isInView
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.asList

// find first Element that is fully in view on the left, then scroll to the element before it
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
)

internal class ScrollButtonStore : RootStore<ScrollButton>(ScrollButton())
