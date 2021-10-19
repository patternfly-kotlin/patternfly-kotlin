package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.Id
import org.patternfly.dom.removeFromParent
import org.w3c.dom.Element
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event

// TODO Implement overflow and draggable chips
// ------------------------------------------------------ factory

/**
 * Creates a [Chip] component.
 *
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param build a lambda expression for setting up the component itself
 */
public fun RenderContext.chip(
    baseClass: String? = null,
    id: String? = null,
    build: Chip.() -> Unit
) {
    Chip().apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [chip](https://www.patternfly.org/v4/components/chip/design-guidelines) component.
 *
 * A chip is used to communicate a value or a set of attribute-value pairs within workflows that involve filtering a set of objects.
 *
 * If the chip is not created as read-only, the chip is closeable.
 *
 * @sample org.patternfly.sample.ChipSample.basicChips
 */
public class Chip :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Div, HTMLDivElement> by ElementMixin(),
    WithEvents<HTMLDivElement> by EventMixin(),
    WithTitle<Span, HTMLSpanElement> by TitleMixin(),
    WithClosable<HTMLButtonElement> by ClosableMixin() {

    private var readOnly: Boolean = false
    private var badge: (Badge.() -> Unit)? = null
    private lateinit var root: Tag<HTMLElement>

    public fun readOnly(readOnly: Boolean) {
        this.readOnly = readOnly
    }

    public fun badge(badge: Badge.() -> Unit) {
        this.badge = badge
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = div(
                baseClass = classes {
                    +ComponentType.Chip
                    +("read-only".modifier() `when` readOnly)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Chip)
                ariaContext.applyTo(this)
                element(this)
                events(this)

                val textId = Id.unique(ComponentType.Chip.id, "txt")
                span(baseClass = "chip".component("text"), id = textId) {
                    title.asText()
                }
                badge?.let { bdg ->
                    badge {
                        read(true)
                        bdg(this)
                    }
                }
                if (!readOnly) {
                    pushButton(plain) {
                        icon("times".fas())
                        aria["label"] = "Remove"
                        aria["labelledby"] = textId
                        domNode.addEventListener(Events.click.name, this@Chip::close)
                        closeAction?.invoke(this)
                    }
                }
            }
        }
    }

    private fun close(event: Event) {
        (event.target as Element).removeEventListener(Events.click.name, ::close)
        root.domNode.removeFromParent()
    }
}
