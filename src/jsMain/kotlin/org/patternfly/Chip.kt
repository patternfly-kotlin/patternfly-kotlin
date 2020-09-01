package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import org.patternfly.Modifier.plain
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

fun HtmlElements.pfChip(
    text: String,
    readOnly: Boolean = false,
    classes: String? = null,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(text, readOnly, classes), content)

fun HtmlElements.pfChip(
    text: String,
    readOnly: Boolean = false,
    modifier: Modifier,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(text, readOnly, modifier.value), content)

fun Chip.pfBadge(
    min: Int = 0,
    max: Int = 999,
    classes: String? = null,
    content: Badge.() -> Unit = {}
) {
    insertBadge(this, register(Badge(min, max, classes), content))
}

fun Chip.pfBadge(
    min: Int = 0,
    max: Int = 999,
    modifier: Modifier,
    content: Badge.() -> Unit = {}
) {
    insertBadge(this, register(Badge(min, max, modifier.value), content))
}

private fun insertBadge(chip: Chip, badge: Badge) {
    chip.domNode.querySelector(By.classname("chip".component("text")))?.let {
        it.parentElement?.insertBefore(badge.domNode, it.nextElementSibling)
    }
}

// ------------------------------------------------------ tag

@OptIn(ExperimentalCoroutinesApi::class)
class Chip internal constructor(text: String, readOnly: Boolean, classes: String?) :
    PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
        +ComponentType.Chip
        +(if (readOnly) "read-only".modifier() else null)
        +classes
    }) {

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
        val textId = Id.unique(ComponentType.Chip.id)
        span(id = textId, baseClass = "chip".component("text")) {
            +text
            domNode.title = text
        }
        if (!readOnly) {
            closeButton = pfButton(plain) {
                pfIcon("times".fas())
                aria["label"] = "Remove"
                aria["labelledby"] = textId
                domNode.addEventListener(Events.click.name, { this@Chip.domNode.removeFromParent() })
            }
        }
    }
}

