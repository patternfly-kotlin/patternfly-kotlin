package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.Id
import org.patternfly.dom.removeFromParent
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// TODO Implement overflow and draggable chips
// ------------------------------------------------------ factory

/**
 * Creates a [Chip] component.
 *
 * @param title the chip's title
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.chip(
    title: String? = null,
    baseClass: String? = null,
    id: String? = null,
    context: Chip.() -> Unit
) {
    Chip(title).apply(context).render(this, baseClass, id)
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
public open class Chip(title: String?) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    private var readOnly: Boolean = false
    private var badge: (Badge.() -> Unit)? = null
    private var badgeCount: Int = 0
    private var badgeMin: Int = Badge.BADGE_MIN
    private var badgeMax: Int = Badge.BADGE_MAX
    private lateinit var root: Tag<HTMLElement>
    private var closable: Boolean = false
    private val closeStore: RootStore<MouseEvent> = storeOf(MouseEvent(""))

    init {
        title?.let { title(it) }
    }

    /**
     * [Flow] for the close events of this chip.
     *
     * @sample org.patternfly.sample.ChipSample.close
     */
    public val closes: Flow<MouseEvent> = closeStore.data.drop(1)

    /**
     * Whether this chip can be closed.
     */
    public fun closable(closable: Boolean) {
        this.closable = closable
    }

    /**
     * Whether this chip is read-only.
     */
    public fun readOnly(readOnly: Boolean) {
        this.readOnly = readOnly
    }

    /**
     * Adds a [Badge] to this chip.
     */
    public fun badge(
        count: Int = 0,
        min: Int = Badge.BADGE_MIN,
        max: Int = Badge.BADGE_MAX,
        context: Badge.() -> Unit = {}
    ) {
        this.badgeCount = count
        this.badgeMin = min
        this.badgeMax = max
        this.badge = context
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
                applyElement(this)
                applyEvents(this)

                val textId = Id.unique(ComponentType.Chip.id, "txt")
                span(baseClass = "chip".component("text"), id = textId) {
                    applyTitle(this)
                }
                badge?.let { bdg ->
                    badge(badgeCount, badgeMin, badgeMax) {
                        read(true)
                        bdg(this)
                    }
                }
                if (!readOnly && closable) {
                    pushButton(plain) {
                        icon("times".fas())
                        aria["label"] = "Remove"
                        aria["labelledby"] = textId
                        domNode.addEventListener(Events.click.name, this@Chip::removeFromParent)
                        clicks.map { it } handledBy this@Chip.closeStore.update
                    }
                }
            }
        }
    }

    private fun removeFromParent(event: Event) {
        event.target?.removeEventListener(Events.click.name, ::removeFromParent)
        if (root.scope.contains(Scopes.CHIP_GROUP)) {
            root.domNode.parentElement.removeFromParent()
        } else {
            root.domNode.removeFromParent()
        }
    }
}
