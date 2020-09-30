package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

fun HtmlElements.pfChip(
    readOnly: Boolean = false,
    id: String? = null,
    classes: String? = null,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(readOnly, id = id, classes = classes), content)

fun Chip.pfBadge(
    min: Int = 0,
    max: Int = 999,
    id: String? = null,
    classes: String? = null,
    content: Badge.() -> Unit = {}
) {
    insertBadge(this, register(Badge(min, max, id = id, classes = classes), content))
}

private fun insertBadge(chip: Chip, badge: Badge) {
    chip.domNode.querySelector(By.classname("chip".component("text")))?.let {
        it.parentElement?.insertBefore(badge.domNode, it.nextElementSibling)
    }
}

// ------------------------------------------------------ tag

class Chip internal constructor(readOnly: Boolean, id: String?, classes: String?) :
    PatternFlyComponent<HTMLDivElement>,
    WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.Chip
        +("read-only".modifier() `when` readOnly)
        +classes
    }) {

    private val textId = Id.unique(ComponentType.Chip.id, "txt")
    private val textElement = span(id = textId, baseClass = "chip".component("text"), content = {})
    private var closeButton: Button? = null
    val closes: Listener<MouseEvent, HTMLButtonElement> by lazy {
        if (closeButton != null) {
            Listener(callbackFlow {
                val listener: (Event) -> Unit = {
                    offer(it.unsafeCast<MouseEvent>())
                }
                this@Chip.closeButton?.domNode?.addEventListener(Events.click.name, listener)
                awaitClose { this@Chip.closeButton?.domNode?.removeEventListener(Events.click.name, listener) }
            })
        } else {
            Listener(emptyFlow())
        }
    }

    init {
        markAs(ComponentType.Chip)
        register(textElement, {})
        if (!readOnly) {
            closeButton = pfButton("plain".modifier()) {
                pfIcon("times".fas())
                aria["label"] = "Remove"
                aria["labelledby"] = this@Chip.textId
                domNode.addEventListener(Events.click.name, { this@Chip.domNode.removeFromParent() })
            }
        }
    }

    override fun delegate(): HTMLSpanElement = textElement.domNode

    override fun appendText(text: String): Node {
        textElement.domNode.title = text
        return super.appendText(text)
    }
}
