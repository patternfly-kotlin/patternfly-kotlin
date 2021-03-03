package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.lenses.IdProvider
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

// ------------------------------------------------------ dsl

/**
 * Creates a new [Tabs] component.
 *
 * @param store the tab items store
 * @param box whether to use box styling
 * @param filled whether to use the filled tab list layout
 * @param vertical whether to use vertical tab styling
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.TabsSample.tabs
 */
public fun <T> RenderContext.tabs(
    store: TabStore<T> = TabStore(),
    box: Boolean = false,
    filled: Boolean = false,
    vertical: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Tabs<T>.() -> Unit = {}
): Tabs<T> = register(
    Tabs(store, box = box, filled = filled, vertical = vertical, id = id, baseClass = baseClass, job),
    content
)

/**
 * Creates and returns a list of [TabItem]s.
 *
 * @param block code block for adding the tab items.
 */
public fun <T> items(block: TabItemsBuilder<T>.() -> Unit = {}): List<TabItem<T>> =
    TabItemsBuilder<T>().apply(block).build()

/**
 * Starts a block to add tab items to the [TabStore].
 *
 * @receiver the [Tabs] component.
 *
 * @param block code block for adding the tab items.
 */
public fun <T> Tabs<T>.items(block: TabItemsBuilder<T>.() -> Unit = {}) {
    val tabItems = TabItemsBuilder<T>().apply(block).build()
    store.update(tabItems)
}

/**
 * Starts a block to add tab items to the [TabStore].
 *
 * @receiver the [TabStore].
 *
 * @param block code block for adding the tab items.
 *
 * @sample org.patternfly.sample.TabsSample.store
 */
public fun <T> TabStore<T>.updateItems(block: TabItemsBuilder<T>.() -> Unit = {}) {
    val tabItems = TabItemsBuilder<T>().apply(block).build()
    update(tabItems)
}

/**
 * Adds a tab item.
 *
 * @param item the wrapped data
 * @param selected whether this tab item is selected
 * @param icon an optional icon
 * @param content lambda for setting up the tab content
 */
public fun <T> TabItemsBuilder<T>.item(
    item: T,
    selected: Boolean = false,
    icon: (Span.() -> Unit)? = null,
    content: ComponentDisplay<TabContent<T>, T> = {}
) {
    tabItems.add(TabItem(item, selected = selected, icon = icon, content = content))
}

// ------------------------------------------------------ tag

/**
 * PatternFly [tabs](https://www.patternfly.org/v4/components/tabs/design-guidelines) component.
 *
 * A tab component creates a set of tabs to organize content on a page. Unlike the PatternFly counterpart, this class combines both the [tabs](https://www.patternfly.org/v4/components/tabs/design-guidelines) **and** the [tab content](https://www.patternfly.org/v4/components/tab-content/design-guidelines) component.
 *
 * Tabs are managed by a [TabStore] and rendered by display functions (see below). Each tab is typed to a specific type `T` and wrapped inside an [TabItem] instance.
 *
 * ### Adding items
 *
 * Tabs can be added by using the [TabStore] or by using the DSL.
 *
 * ### Rendering items
 *
 * The actual tabs are rendered by the [tabDisplay] function, which defaults to `{ +it.toString() }`. The function to render the tab content can be specified using two different ways:
 *
 * 1. The [contentDisplay] function
 * 1. The [TabItem.content] function
 *
 * The latter takes precedence over the former.
 *
 * @sample org.patternfly.sample.TabsSample.tabs
 * @sample org.patternfly.sample.TabsSample.store
 */
public class Tabs<T> internal constructor(
    public val store: TabStore<T>,
    public val box: Boolean,
    public val filled: Boolean,
    public val vertical: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, job = job) {

    private lateinit var ul: Ul
    private val scrollStore = ScrollButtonStore()
    private var tabDisplay: ComponentDisplay<Span, T> = { +it.toString() }
    private var contentDisplay: ComponentDisplay<TabContent<T>, T> = {}

    init {
        markAs(ComponentType.Tabs)

        div(
            baseClass = classes {
                +"tabs".component()
                +("box".modifier() `when` box)
                +("fill".modifier() `when` filled)
                +("vertical".modifier() `when` vertical)
                +baseClass
            }
        ) {
            classMap(
                this@Tabs.scrollStore.data.map {
                    mapOf("scrollable".modifier() to (!this@Tabs.vertical && it.showButtons))
                }
            )
            button(baseClass = "tabs".component("scroll", "button")) {
                aria["label"] = "Scroll left"
                disabled(this@Tabs.scrollStore.data.map { it.disableLeft })
                aria["hidden"] = this@Tabs.scrollStore.data.map { it.disableLeft.toString() }
                domNode.onclick = { this@Tabs.ul.domNode.scrollLeft() }
                icon("angle-left".fas())
            }
            this@Tabs.ul = ul(baseClass = "tabs".component("list")) {
                // update scroll buttons, when scroll event has been fired
                // e.g. by scrollLeft() or scrollRight()
                scrolls.map { domNode.updateScrollButtons() }
                    .filterNotNull()
                    .handledBy(this@Tabs.scrollStore.update)

                this@Tabs.store.data.renderEach({ this@Tabs.selectId(it) }) { tab ->
                    li(
                        baseClass = classes {
                            +"tabs".component("item")
                            +("current".modifier() `when` tab.selected)
                        }
                    ) {
                        button(id = this@Tabs.tabId(tab.item), baseClass = "tabs".component("link")) {
                            aria["controls"] = this@Tabs.contentId(tab.item)
                            clicks.map { tab } handledBy this@Tabs.store.selectTab
                            if (tab.icon != null) {
                                span(baseClass = "tabs".component("item", "icon")) {
                                    tab.icon.invoke(this)
                                }
                            }
                            span(baseClass = "tabs".component("item", "text")) {
                                this@Tabs.tabDisplay(this, tab.item)
                            }
                        }
                    }
                }
            }
            button(baseClass = "tabs".component("scroll", "button")) {
                aria["label"] = "Scroll right"
                disabled(this@Tabs.scrollStore.data.map { it.disableRight })
                aria["hidden"] = this@Tabs.scrollStore.data.map { it.disableRight.toString() }
                domNode.onclick = { this@Tabs.ul.domNode.scrollRight() }
                icon("angle-right".fas())
            }
        }

        store.data.renderShifted(
            1,
            this,
            { selectId(it) },
            { tabItem ->
                register(
                    TabContent(
                        tabItem.item,
                        this@Tabs.tabId(tabItem.item),
                        this@Tabs.contentId(tabItem.item),
                        job
                    ),
                    { tabContent ->
                        if (!tabItem.selected) {
                            tabContent.attr("hidden", "")
                        }
                        // tab content rendering order:
                        // (1) content display function of this Tabs instance
                        // (2) content display function from the TabItem
                        this@Tabs.contentDisplay.invoke(tabContent, tabContent.item)
                        tabItem.content.invoke(tabContent, tabContent.item)
                    },
                )
            }
        )

        // update scroll buttons, when tab items have been updated
        store.data.map { ul.domNode.updateScrollButtons() }.filterNotNull() handledBy scrollStore.update

        // update scroll buttons, when window has been resized
        callbackFlow {
            val listener: (Event) -> Unit = { offer(it) }
            window.addEventListener(Events.resize.name, listener)
            awaitClose { domNode.removeEventListener(Events.resize.name, listener) }
        }.map { ul.domNode.updateScrollButtons() }.filterNotNull() handledBy scrollStore.update
    }

    /**
     * Sets the function used to render the tabs.
     */
    public fun tabDisplay(display: ComponentDisplay<Span, T>) {
        this.tabDisplay = display
    }

    /**
     * Sets the function used to render the tab panels.
     */
    public fun contentDisplay(display: ComponentDisplay<TabContent<T>, T>) {
        this.contentDisplay = display
    }

    private fun selectId(tabItem: TabItem<T>) = Id.build(store.identifier(tabItem.item), tabItem.selected.toString())
    private fun tabId(item: T) = Id.build(store.identifier(item), "tab")
    private fun contentId(item: T) = Id.build(store.identifier(item), "cnt")
}

/**
 * PatternFly [tab content](https://www.patternfly.org/v4/components/tab-content/design-guidelines) component.
 *
 * A tab content component must always be used with the [Tabs] component.
 */
public class TabContent<T> internal constructor(public val item: T, tabId: String, contentId: String, job: Job) :
    PatternFlyComponent<HTMLElement>,
    TextElement("section", id = contentId, baseClass = classes("tab".component("content")), job) {

    init {
        aria["labeledby"] = tabId
        attr("role", "tabpanel")
        attr("tabindex", "0")
    }
}

// ------------------------------------------------------ store

/**
 * Store for a list of [TabItem]s.
 */
public class TabStore<T>(public val identifier: IdProvider<T, String> = { Id.build(it.toString()) }) :
    RootStore<List<TabItem<T>>>(emptyList()) {

    internal val selectTab: EmittingHandler<TabItem<T>, TabItem<T>> =
        handleAndEmit { items, tab ->
            emit(tab)
            items.map {
                if (identifier(it.unwrap()) == identifier(tab.unwrap()))
                    it.copy(selected = true)
                else
                    it.copy(selected = false)
            }
        }

    public val selectItem: EmittingHandler<T, TabItem<T>> =
        handleAndEmit { items, item ->
            items.find { identifier(it.unwrap()) == identifier(item) }?.let { emit(it) }
            items.map {
                if (identifier(it.item) == identifier(item))
                    it.copy(selected = true)
                else
                    it.copy(selected = false)
            }
        }

    /**
     * Flow with the last selected tab item.
     */
    public val selects: Flow<TabItem<T>> = flowOf(selectTab, selectItem).flattenMerge()
}

// ------------------------------------------------------ types

/**
 * Wrapper for the data of a tab item.
 */
public data class TabItem<T>(
    override val item: T,
    public val selected: Boolean = false,
    internal val icon: (Span.() -> Unit)? = null,
    internal val content: ComponentDisplay<TabContent<T>, T> = {}
) : HasItem<T>

/**
 * Builder for a [TabItem].
 */
public class TabItemsBuilder<T> internal constructor() {
    internal val tabItems: MutableList<TabItem<T>> = mutableListOf()

    @Suppress("NestedBlockDepth")
    internal fun build(): List<TabItem<T>> {
        return when (tabItems.count { it.selected }) {
            0 -> {
                // no selection -> select first
                tabItems.mapIndexed { index, tab ->
                    if (index == 0) tab.copy(selected = true) else tab
                }
            }
            1 -> {
                // one selection -> we're fine
                tabItems
            }
            else -> {
                // more than one selection -> take first selection, unselect remaining
                var hasSelection = false
                val modified = mutableListOf<TabItem<T>>()
                for (tabItem in tabItems) {
                    if (hasSelection) {
                        if (tabItem.selected) {
                            modified.add(tabItem.copy(selected = false))
                        } else {
                            modified.add(tabItem)
                        }
                    } else {
                        modified.add(tabItem)
                    }
                    hasSelection = hasSelection || tabItem.selected
                }
                modified
            }
        }
    }
}
