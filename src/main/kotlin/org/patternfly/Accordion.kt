package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.flow.Flow
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates an [Accordion] component.
 *
 * @param singleExpand whether only one item can be expanded at a time
 * @param fixed whether the AccordionItems use a fixed height
 * @param bordered whether to draw a border between the [AccordionItem]s
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.accordion(
    singleExpand: Boolean = false,
    fixed: Boolean = false,
    bordered: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Accordion.() -> Unit = {}
) {
    Accordion(
        singleExpand = singleExpand,
        fixed = fixed,
        bordered = bordered
    ).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [accordion](https://www.patternfly.org/v4/components/accordion/design-guidelines/) component.
 *
 * An accordion is used to deliver a lot of content in a small space, allowing the user to expand and collapse the component to show or hide information.
 *
 * The [items][AccordionItem] can be added statically and/or by using a store. See the samples for more details.

 * @sample org.patternfly.sample.AccordionSample.staticItems
 * @sample org.patternfly.sample.AccordionSample.dynamicItems
 */
public open class Accordion(
    private var singleExpand: Boolean,
    private var fixed: Boolean,
    private var bordered: Boolean
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val itemStore: HeadTailItemStore<AccordionItem> = HeadTailItemStore()

    /**
     * whether only one [AccordionItem] can be expanded at a time
     */
    public fun singleExpand(singleExpand: Boolean) {
        this.singleExpand = singleExpand
    }

    /**
     * whether the [AccordionItem]s use a fixed height
     */
    public fun fixed(fixed: Boolean) {
        this.fixed = fixed
    }

    /**
     * whether to draw a border between the [AccordionItem]s
     */
    public fun bordered(bordered: Boolean) {
        this.bordered = bordered
    }

    /**
     * Adds a [AccordionItem].
     */
    public fun item(
        id: String = Id.unique(ComponentType.Accordion.id, "itm"),
        title: String? = null,
        context: AccordionItem.() -> Unit = {}
    ) {
        itemStore.add(AccordionItem(id, title).apply(context))
    }

    /**
     * Adds the items from the specified store.
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: AccordionItemScope.(T) -> AccordionItem
    ) {
        items(values.data, idProvider, display)
    }

    /**
     * Adds the items from the specified flow.
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        display: AccordionItemScope.(T) -> AccordionItem
    ) {
        itemStore.collect(values) { valueList ->
            itemStore.update(valueList) { value ->
                AccordionItemScope(idProvider(value)).run {
                    display.invoke(this, value)
                }
            }
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
                applyElement(this)
                applyEvents(this)

                itemStore.allItems.render(into = this) { items ->
                    items.forEach { item ->
                        renderItem(this, item)
                        expandItem(item)
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: AccordionItem) {
        with(context) {
            dt {
                button(baseClass = "accordion".component("toggle")) {
                    with(item.expandedStore) {
                        toggleAriaExpanded()
                        toggleExpanded()
                    }
                    clicks handledBy item.expandedStore.toggle
                    if (singleExpand) {
                        domNode.addEventListener(Events.click.name, { collapseAllBut(item) })
                    }
                    item.applyEvents(this)
                    span(baseClass = "accordion".component("toggle", "text")) {
                        item.applyTitle(this)
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
                with(item.expandedStore) {
                    hideIfCollapsed()
                    toggleExpanded()
                }
                item.content?.let { content ->
                    div(
                        baseClass = classes(
                            "accordion".component("expanded", "content", "body"),
                            content.baseClass
                        ),
                        id = content.id
                    ) {
                        content.context(this)
                    }
                }
            }
        }
    }

    private fun expandItem(item: AccordionItem) {
        if (item.initiallyExpanded) {
            item.expandedStore.expand(Unit)
            if (singleExpand) {
                collapseAllBut(item)
            }
        }
    }

    private fun collapseAllBut(item: AccordionItem) {
        itemStore.staticItems.filter { it.id != item.id }.forEach { it.expandedStore.collapse(Unit) }
    }
}

// ------------------------------------------------------ item & store

/**
 * DSL scope class to create [AccordionItem]s when using [Accordion.items] functions.
 *
 * @sample org.patternfly.sample.AccordionSample.dynamicItems
 */
public class AccordionItemScope internal constructor(internal var id: String) {

    /**
     * Creates and returns a new [AccordionItem].
     */
    public fun item(title: String? = null, context: AccordionItem.() -> Unit = {}): AccordionItem =
        AccordionItem(id, title).apply(context)
}

/**
 * An item in an [Accordion] component. The item consists of a typed data and a rendered content. Use string as type, if you just want to build a normal accordion:
 *
 * @sample org.patternfly.sample.AccordionSample.staticItems
 */
public class AccordionItem internal constructor(internal val id: String, title: String?) :
    WithExpandedStore by ExpandedStoreMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var initiallyExpanded: Boolean = false
    internal var content: SubComponent<Div>? = null

    init {
        title?.let { this.title(it) }
    }

    /**
     * Sets the render function for the content of the accordion item.
     */
    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        this.content = SubComponent(baseClass, id, context)
    }

    /**
     * Whether the item is initially expanded
     */
    public fun expanded(expanded: Boolean) {
        this.initiallyExpanded = expanded
    }
}
