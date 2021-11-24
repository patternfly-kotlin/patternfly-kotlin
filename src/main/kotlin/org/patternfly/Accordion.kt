package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates an [Accordion] component.
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
    store: Store<List<T>>? = null,
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
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val items: MutableList<AccordionItem<T>> = mutableListOf()
    private var display: ((T) -> AccordionItem<T>)? = null
    private var selectionStore: RootStore<T?> = storeOf(null)
    public val selections: Flow<T> = selectionStore.data.mapNotNull { it }

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
     * Adds an [AccordionItem].
     */
    public fun item(data: T, context: AccordionItem<T>.() -> Unit = {}): AccordionItem<T> =
        AccordionItem(data).apply(context).also {
            items.add(it)
        }

    /**
     * Defines how to render [AccordionItem]s when using a store.
     */
    public fun display(display: (T) -> AccordionItem<T>) {
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
                applyElement(this)
                applyEvents(this)

                if (store != null) {
                    store.data.render { list ->
                        list.forEach { data ->
                            val dsp = display ?: { item(data) }
                            val item = dsp(data)
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

    private fun renderItem(context: RenderContext, item: AccordionItem<T>) {
        with(context) {
            dt {
                button(baseClass = "accordion".component("toggle")) {
                    attr("aria-expanded", item.expandedStore.data.map { it.toString() })
                    classMap(
                        item.expandedStore.data.map { expanded ->
                            mapOf("expanded".modifier() to expanded)
                        }
                    )
                    clicks.map { item.data } handledBy selectionStore.update
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
                attr("hidden", item.expandedStore.data.map { !it })
                classMap(item.expandedStore.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
                item.content?.let { content ->
                    div(baseClass = "accordion".component("expanded", "content", "body")) {
                        content(this)
                    }
                }
            }
        }
    }

    private fun expandItem(item: AccordionItem<T>) {
        if (item.initiallyExpanded) {
            item.expandedStore.expand(Unit)
            if (singleExpand) {
                collapseAllBut(item)
            }
        }
    }

    private fun collapseAllBut(item: AccordionItem<T>) {
        items.filter { it.id != item.id }.forEach { it.expandedStore.collapse(Unit) }
    }
}

// ------------------------------------------------------ item

/**
 * An item in an [Accordion] component. The item consists of a title and a content.
 */
public class AccordionItem<T> internal constructor(public val data: T) :
    WithExpandedStore by ExpandedStoreMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal val id: String = Id.unique(ComponentType.Accordion.id, "itm")
    internal var initiallyExpanded: Boolean = false
    internal var content: (RenderContext.() -> Unit)? = null

    init {
        this.title(data.toString())
    }

    public fun content(content: RenderContext.() -> Unit) {
        this.content = content
    }

    /**
     * Whether the item is initially expanded
     */
    public fun expanded(expanded: Boolean) {
        this.initiallyExpanded = expanded
    }
}
