package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.w3c.dom.HTMLDListElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates an [Accordion] component based on a `<dl>` element with nested `<dt>` and `<dd>` elements.
 *
 * @param singleExpand whether only one [AccordionContent] can be expanded at a time
 * @param fixed whether [AccordionContent] uses a fixed height
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AccordionSample.accordionDl
 */
public fun RenderContext.accordionDl(
    singleExpand: Boolean = true,
    fixed: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Accordion<HTMLDListElement>.() -> Unit = {}
): Accordion<HTMLDListElement> =
    register(
        Accordion(
            definitionList = true,
            headingLevel = 3,
            singleExpand = singleExpand,
            fixed = fixed,
            id = id,
            baseClass = baseClass,
            job = job
        ),
        content
    )

/**
 * Creates an [Accordion] component based on `<div>` element with nested `<h1>` - `<h6>` and `<div>` elements.
 *
 * @param headingLevel the level used for the heading elements
 * @param singleExpand whether only one [AccordionContent] can be expanded at a time
 * @param fixed whether [AccordionContent] uses a fixed height
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AccordionSample.accordionDiv
 */
public fun RenderContext.accordionDiv(
    headingLevel: Int = 3,
    singleExpand: Boolean = true,
    fixed: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Accordion<HTMLDivElement>.() -> Unit = {}
): Accordion<HTMLDivElement> =
    register(
        Accordion(
            definitionList = false,
            headingLevel = headingLevel,
            singleExpand = singleExpand,
            fixed = fixed,
            id = id,
            baseClass = baseClass,
            job = job
        ),
        content
    )

/**
 * Creates an [AccordionItem] 'component'. An [AccordionItem] is not a real component, but an umbrella around a ([AccordionTitle], [AccordionContent]) pair.
 *
 * @param expanded whether the item is initially expanded
 * @param content a lambda expression for setting up the [AccordionTitle] and [AccordionContent] components
 */
public fun <E : HTMLElement> Accordion<E>.accordionItem(
    expanded: Boolean = false,
    content: AccordionItem<E>.() -> Unit = {}
) {
    val item = AccordionItem(this, job)
    items.add(item)
    content(item)
    if (expanded) {
        item.expanded.expand(Unit)
        if (singleExpand) {
            collapseAllBut(item)
        }
    }
}

/**
 * Creates an [AccordionTitle] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the title
 */
public fun <E : HTMLElement> AccordionItem<E>.accordionTitle(
    id: String? = null,
    baseClass: String? = null,
    content: Span.() -> Unit = {}
): AccordionTitle<E> = register(AccordionTitle(this, id, baseClass, job, content), {})

/**
 * Creates an [AccordionContent] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the content
 */
public fun <E : HTMLElement> AccordionItem<E>.accordionContent(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): AccordionContent<E> = register(AccordionContent(this, id, baseClass, job, content), {})

// ------------------------------------------------------ tag

/**
 * PatternFly [accordion](https://www.patternfly.org/v4/components/accordion/design-guidelines/) component.
 *
 * An accordion is used to deliver a lot of content in a small space, allowing the user to expand and collapse the component to show or hide information.
 *
 * The accordion component comes in two variations:
 *
 * 1. Based on a `<dl>` element: In that case the accordion component uses `<dt>` elements for the [AccordionTitle] components and `<dd>` elements for the [AccordionContent] components.
 *
 * 1. Based on a `<div>` element: In that case the accordion component uses `<h1>` - `<h6>` elements for the [AccordionTitle] components and `<div>` elements for the [AccordionContent] components.
 *
 * @sample org.patternfly.sample.AccordionSample.accordionDl
 */
public class Accordion<E : HTMLElement> internal constructor(
    internal val definitionList: Boolean,
    internal val headingLevel: Int,
    internal val singleExpand: Boolean,
    internal val fixed: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<E>,
    WithText<E>,
    Tag<E>(
        tagName = if (definitionList) "dl" else "div",
        id = id,
        baseClass = classes {
            +ComponentType.Accordion
            +baseClass
        },
        job
    ) {

    internal val items: MutableList<AccordionItem<E>> = mutableListOf()

    init {
        markAs(ComponentType.Accordion)
    }

    internal fun collapseAllBut(item: AccordionItem<E>) {
        items.filter { it.id != item.id }.forEach { it.expanded.collapse(Unit) }
    }
}

/**
 * Artificial component to group a ([AccordionTitle], [AccordionContent]) pair. The component uses the [Accordion] component to implement [RenderContext].
 */
public class AccordionItem<E : HTMLElement>(internal val accordion: Accordion<E>, override val job: Job) :
    RenderContext by accordion {

    internal val id: String = Id.unique(ComponentType.Accordion.id, "itm")
    internal val expanded: ExpandedStore = ExpandedStore()
}

/**
 * Accordion title component.
 */
public class AccordionTitle<E : HTMLElement>(
    item: AccordionItem<E>,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Span.() -> Unit
) : WithText<HTMLElement>,
    Tag<HTMLElement>(
        tagName = if (item.accordion.definitionList) "dt" else "h${item.accordion.headingLevel}",
        job = job
    ) {

    init {
        button(baseClass = "accordion".component("toggle")) {
            attr("aria-expanded", item.expanded.data.map { it.toString() })
            classMap(item.expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
            clicks handledBy item.expanded.toggle
            if (item.accordion.singleExpand) {
                domNode.addEventListener(Events.click.name, { item.accordion.collapseAllBut(item) })
            }

            span(id = id, baseClass = classes("accordion".component("toggle", "text"), baseClass)) {
                content(this)
            }
            span(baseClass = "accordion".component("toggle", "icon")) {
                icon("angle-right".fas())
            }
        }
    }
}

/**
 * Accordion content component.
 */
public class AccordionContent<E : HTMLElement>(
    item: AccordionItem<E>,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Div.() -> Unit
) : WithText<HTMLElement>,
    Tag<HTMLElement>(
        tagName = if (item.accordion.definitionList) "dd" else "div",
        baseClass = classes {
            +"accordion".component("expanded", "content")
            +("fixed".modifier() `when` item.accordion.fixed)
        },
        job = job
    ) {

    init {
        attr("hidden", item.expanded.data.map { !it })
        classMap(item.expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
        div(
            id = id,
            baseClass = classes("accordion".component("expanded", "content", "body"), baseClass)
        ) {
            content(this)
        }
    }
}
