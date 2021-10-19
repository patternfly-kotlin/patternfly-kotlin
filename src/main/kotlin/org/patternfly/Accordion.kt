package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Dl
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.w3c.dom.HTMLDListElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

// ------------------------------------------------------ factory

/**
 * Creates an [Accordion] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AccordionSample.accordion
 */
public fun RenderContext.accordion(
    baseClass: String? = null,
    id: String? = null,
    build: Accordion.() -> Unit
) {
    Accordion().apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [accordion](https://www.patternfly.org/v4/components/accordion/design-guidelines/) component.
 *
 * An accordion is used to deliver a lot of content in a small space, allowing the user to expand and collapse the component to show or hide information.
 *
 * @sample org.patternfly.sample.AccordionSample.accordion
 */
public class Accordion :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Dl, HTMLDListElement> by ElementMixin(),
    WithEvents<HTMLDListElement> by EventMixin() {

    private var fixed: Boolean = false
    private var singleExpand: Boolean = false
    private var bordered: Boolean = false
    private val items: MutableList<AccordionItem> = mutableListOf()

    /**
     * whether the [AccordionItem]s use a fixed height
     */
    public fun fixed(fixed: Boolean) {
        this.fixed = fixed
    }

    /**
     * whether only one [AccordionItem] can be expanded at a time
     */
    public fun singleExpand(singleExpand: Boolean) {
        this.singleExpand = singleExpand
    }

    /**
     * whether to draw a border between the [AccordionItem]s
     */
    public fun bordered(bordered: Boolean) {
        this.bordered = bordered
    }

    /**
     * Adds an [AccordionItem].
     *
     * @param build a lambda expression for setting up the accordion item
     */
    public fun item(build: AccordionItem.() -> Unit) {
        AccordionItem().apply(build).run {
            items.add(this)
        }
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            dl(
                baseClass = classes {
                    +ComponentType.Accordion
                    +("bordered".modifier() `when` bordered)
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Accordion)
                ariaContext.applyTo(this)
                element(this)
                events(this)

                items.forEach { item ->
                    renderItem(this, item)
                    if (item.initiallyExpanded) {
                        item.expanded.expand(Unit)
                        if (singleExpand) {
                            collapseAllBut(item)
                        }
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: AccordionItem) {
        with(context) {
            dt {
                button(baseClass = "accordion".component("toggle")) {
                    attr("aria-expanded", item.expanded.data.map { it.toString() })
                    classMap(
                        item.expanded.data.map { expanded ->
                            mapOf("expanded".modifier() to expanded)
                        }
                    )
                    clicks handledBy item.expanded.toggle
                    if (singleExpand) {
                        domNode.addEventListener(Events.click.name, { collapseAllBut(item) })
                    }
                    span(baseClass = "accordion".component("toggle", "text")) {
                        item.unsafeCast<TitleMixin<Span, HTMLSpanElement>>().title.asText()
                    }
                    span(baseClass = "accordion".component("toggle", "icon")) {
                        icon("angle-right".fas())
                    }
                }
            }
            dd(
                baseClass = classes {
                    +"accordion".component("expanded", "content")
                    +("fixed".modifier() `when` fixed)
                }
            ) {
                attr("hidden", item.expanded.data.map { !it })
                classMap(item.expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
                item.content?.let { content ->
                    div(baseClass = "accordion".component("expanded", "content", "body")) {
                        content(this)
                    }
                }
            }
        }
    }

    private fun collapseAllBut(item: AccordionItem) {
        items.filter { it.id != item.id }.forEach { it.expanded.collapse(Unit) }
    }
}

/**
 * An item in an [Accordion] component. The item consists of a title and a content.
 */
public class AccordionItem :
    Expandable by ExpandedMixin(),
    WithTitle<Span, HTMLSpanElement> by TitleMixin(),
    WithContent<Div, HTMLDivElement> by ContentMixin() {

    internal val id: String = Id.unique(ComponentType.Accordion.id, "itm")
    internal var initiallyExpanded: Boolean = false

    /**
     * Whether the item is initially expanded
     */
    public fun expanded(expanded: Boolean) {
        this.initiallyExpanded = expanded
    }
}
