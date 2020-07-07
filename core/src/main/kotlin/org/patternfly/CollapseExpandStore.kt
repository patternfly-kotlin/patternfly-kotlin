package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Events
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import kotlin.browser.document

internal class CollapseExpandStore(private val root: HTMLElement) : RootStore<Boolean>(false) {

    private val closeHandler: (Event) -> Unit = {
        val clickInside = root.contains(it.target as Node)
        if (!clickInside) {
            removeCloseHandler()
            launch {
                enqueue { false }
            }
        }
    }

    val expand = handle { expanded ->
        val newValue = !expanded
        if (newValue) {
            document.addEventListener(Events.click.name, closeHandler)
        } else {
            removeCloseHandler()
        }
        newValue
    }

    val collapse = handle {
        removeCloseHandler()
        false
    }

    private fun removeCloseHandler() {
        document.removeEventListener(Events.click.name, closeHandler)
    }
}
