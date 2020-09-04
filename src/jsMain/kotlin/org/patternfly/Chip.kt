package org.patternfly

import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.dom.DomMountPoint
import dev.fritz2.dom.Listener
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import org.patternfly.Modifier.plain
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

fun HtmlElements.pfChip(
    readOnly: Boolean = false,
    classes: String? = null,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(readOnly, classes), content)

fun HtmlElements.pfChip(
    readOnly: Boolean = false,
    modifier: Modifier,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(readOnly, modifier.value), content)

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
class Chip internal constructor(readOnly: Boolean, classes: String?) :
    PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
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
            closeButton = pfButton(plain) {
                pfIcon("times".fas())
                aria["label"] = "Remove"
                aria["labelledby"] = this@Chip.textId
                domNode.addEventListener(Events.click.name, { this@Chip.domNode.removeFromParent() })
            }
        }
    }

    override fun text(value: String): Node = setText(value)

    override operator fun String.unaryPlus(): Node = setText(this)

    override fun Flow<String>.bind(preserveOrder: Boolean): SingleMountPoint<WithDomNode<Text>> {
        val upstream = this.map { TextNode(it) }.distinctUntilChanged()
        return DomMountPoint(upstream, textElement.domNode)
    }

    private fun setText(value: String): HTMLSpanElement {
        textElement.domNode.textContent = value
        textElement.domNode.title = value
        return textElement.domNode
    }
}
