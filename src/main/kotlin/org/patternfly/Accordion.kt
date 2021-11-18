package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates an [Accordion] component with static [AccordionItem]s.
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
    context: Accordion<Unit>.() -> Unit = {}
) {
    Accordion<Unit>(
        store = null,
        singleExpand = singleExpand,
        fixed = fixed,
        bordered = bordered
    ).apply(context).render(this, baseClass, id)
}

/**
 * Creates an [Accordion] component with [AccordionItem]s taken from the provided [store].
 *
 * @param store provides data to create [AccordionItem]s using [Accordion.renderItem]
 * @param singleExpand whether only one item can be expanded at a time
 * @param fixed whether the AccordionItems use a fixed height
 * @param bordered whether to draw a border between the [AccordionItem]s
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.accordion(
    store: Store<List<T>>,
    singleExpand: Boolean = false,
    fixed: Boolean = false,
    bordered: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Accordion<T>.() -> Unit = {}
) {
    Accordion(
        store = store,
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
 * @sample org.patternfly.sample.AccordionSample.accordion
 * @sample org.patternfly.sample.AccordionSample.store
 */
public class Accordion<T> internal constructor(
    private val store: Store<List<T>>?,
    private var singleExpand: Boolean,
    private var fixed: Boolean,
    private var bordered: Boolean
) : PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val items: MutableList<AccordionItem> = mutableListOf()
    private var display: ((T) -> AccordionItem)? = null

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
     * Adds a static [AccordionItem].
     */
    public fun item(title: String = "", context: AccordionItem.() -> Unit = {}): AccordionItem =
        AccordionItem(title).apply(context).also {
            items.add(it)
        }

    /**
     * Defines how to render [AccordionItem]s when using a store.
     */
    public fun display(display: (T) -> AccordionItem) {
        this.display = display
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
                aria(this)
                element(this)
                events(this)

                if (store != null) {
                    store.data.render { list ->
                        list.forEach { data ->
                            val display = this@Accordion.display ?: { AccordionItem(data.toString()) }
                            val item = display(data)
                            renderItem(this, item)
                            expandItem(item)
                        }
                    }
                } else {
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
                    attr("aria-expanded", item.expandedStore.data.map { it.toString() })
                    classMap(
                        item.expandedStore.data.map { expanded ->
                            mapOf("expanded".modifier() to expanded)
                        }
                    )
                    clicks handledBy item.expandedStore.toggle
                    if (singleExpand) {
                        domNode.addEventListener(Events.click.name, { collapseAllBut(item) })
                    }
                    item.events(this)
                    span(baseClass = "accordion".component("toggle", "text")) {
                        item.title.asText()
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
                attr("hidden", item.expandedStore.data.map { !it })
                classMap(item.expandedStore.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
                item.content?.let { cnt ->
                    div(
                        baseClass = classes(
                            "accordion".component("expanded", "content", "body"),
                            cnt.baseClass
                        ),
                        id = cnt.id
                    ) {
                        cnt.context(this)
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
        items.filter { it.id != item.id }.forEach { it.expandedStore.collapse(Unit) }
    }
}

// ------------------------------------------------------ item

/**
 * An item in an [Accordion] component. The item consists of a title and a content.
 */
public class AccordionItem internal constructor(title: String) :
    WithExpandedStore by ExpandedStoreMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal val id: String = Id.unique(ComponentType.Accordion.id, "itm")
    internal var initiallyExpanded: Boolean = false
    internal var content: SubComponent<RenderContext>? = null

    init {
        this.title(title)
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit = {}
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
