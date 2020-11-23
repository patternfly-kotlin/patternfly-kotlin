package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.querySelector
import dev.fritz2.elemento.removeFromParent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.Node
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

public fun RenderContext.pfChip(
    readOnly: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Chip.() -> Unit = {}
): Chip = register(Chip(readOnly, id = id, baseClass = baseClass, job), content)

public fun Chip.badge(
    min: Int = 0,
    max: Int = 999,
    id: String? = null,
    baseClass: String? = null,
    content: Badge.() -> Unit = {}
) {
    insertBadge(this, register(Badge(min, max, id = id, baseClass = baseClass, job), content))
}

private fun insertBadge(chip: Chip, badge: Badge) {
    chip.domNode.querySelector(By.classname("chip".component("text")))?.let {
        it.parentElement?.insertBefore(badge.domNode, it.nextElementSibling)
    }
}

// ------------------------------------------------------ tag

@OptIn(ExperimentalCoroutinesApi::class)
public class Chip internal constructor(readOnly: Boolean, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>,
    WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(id = id, baseClass = classes {
        +ComponentType.Chip
        +("read-only".modifier() `when` readOnly)
        +baseClass
    }, job) {

    private val textId = Id.unique(ComponentType.Chip.id, "txt")
    private val textElement = span(id = textId, baseClass = "chip".component("text"), content = {})
    private var closeButton: PushButton? = null
    public val closes: Listener<MouseEvent, HTMLButtonElement> by lazy { subscribe(closeButton, Events.click) }

    init {
        markAs(ComponentType.Chip)
        register(textElement, {})
        if (!readOnly) {
            closeButton = button("plain".modifier()) {
                icon("times".fas())
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
