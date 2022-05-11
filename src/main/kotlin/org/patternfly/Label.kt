package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import org.patternfly.ButtonVariant.plain
import org.patternfly.dom.Id
import org.patternfly.dom.removeFromParent
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent

// TODO Implement overflow and editable labels
// ------------------------------------------------------ factory

/**
 * Creates a [Label] component.
 *
 * @param color the label's color
 * @param title the label's title
 * @param outline whether to use outline style
 * @param compact whether to use compact style
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.label(
    color: Color,
    title: String? = null,
    outline: Boolean = false,
    compact: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Label.() -> Unit = {}
) {
    Label(color, title, outline, compact).apply(context).render(this, baseClass, id)
}

internal fun testLabel() {
    render {
        label(Color.BLUE) {
            +"Test"
        }
    }
}

// ------------------------------------------------------ component

/**
 * PatternFly [label](https://www.patternfly.org/v4/components/label/design-guidelines) component.
 *
 * Use a label when you want to highlight an element on a page to draw attention to it or make it more searchable. Labels can also be used to tag items of the same category. If you want to show a count, use a [badge] instead.
 *
 * @sample org.patternfly.sample.LabelSample.basicLabels
 */
public open class Label(
    private val color: Color,
    title: String?,
    private val outline: Boolean,
    private val compact: Boolean
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    private lateinit var root: Tag<HTMLElement>
    private var hrefValue: String? = null
    private var hrefFlow: Flow<String>? = null
    private var icon: (RenderContext.() -> Unit)? = null
    internal var closable: Boolean = false // must be accessible from LabelGroup
    private val closeStore: RootStore<MouseEvent> = storeOf(MouseEvent(""))
    private val closeHandler: (Event) -> Unit = ::removeFromParent

    init {
        title?.let { title(it) }
    }

    public fun href(href: String) {
        this.hrefValue = href
    }

    public fun href(href: Flow<String>) {
        this.hrefFlow = href
    }

    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        this.icon = {
            icon(iconClass = iconClass, baseClass = "fa-fw") {
                context(this)
            }
        }
    }

    /**
     * [Flow] for the close events of this chip.
     *
     * @sample org.patternfly.sample.LabelSample.close
     */
    public val closes: Flow<MouseEvent> = closeStore.data.drop(1)

    /**
     * Whether this chip can be closed.
     */
    public fun closable(closable: Boolean) {
        this.closable = closable
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            root = span(
                baseClass = classes {
                    +ComponentType.Label
                    +(color.modifier)
                    +("outline".modifier() `when` outline)
                    +("compact".modifier() `when` compact)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Label)
                applyElement(this)
                applyEvents(this)

                val contentId = Id.unique(ComponentType.Label.id, "cnt")
                if (hrefValue != null || hrefFlow != null) {
                    a(baseClass = "label".component("content"), id = contentId) {
                        hrefValue?.let { href(it) }
                        hrefFlow?.let { href(it) }
                        renderIconAndTitle(this)
                    }
                } else {
                    span(baseClass = "label".component("content"), id = contentId) {
                        renderIconAndTitle(this)
                    }
                }
                if (closable) {
                    pushButton(plain) {
                        icon("times".fas())
                        aria["label"] = "Remove"
                        aria["labelledby"] = contentId
                        element {
                            domNode.addEventListener(Events.click.name, this@Label.closeHandler)
                        }
                        events {
                            clicks.map { it } handledBy this@Label.closeStore.update
                        }
                    }
                }
            }
        }
    }

    private fun renderIconAndTitle(context: RenderContext) {
        with(context) {
            icon?.let { icn ->
                span(baseClass = "label".component("icon")) { icn(this) }
            }
            span(baseClass = "label".component("text")) {

                applyTitle(this)
            }
        }
    }

    private fun removeFromParent(event: Event) {
        event.target?.removeEventListener(Events.click.name, closeHandler)
        if (root.scope.contains(Scopes.LABEL_GROUP)) {
            root.domNode.parentElement.removeFromParent()
        } else {
            root.domNode.removeFromParent()
        }
    }
}

/**
 * Color modifier for [Label]s.
 */
public enum class Color(public val modifier: String) {
    GREY(""),
    BLUE("blue".modifier()),
    GREEN("green".modifier()),
    ORANGE("orange".modifier()),
    RED("red".modifier()),
    PURPLE("purple".modifier()),
    CYAN("cyan".modifier()),
}
