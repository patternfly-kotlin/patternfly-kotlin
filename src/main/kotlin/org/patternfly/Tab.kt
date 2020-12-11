package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.isInView
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.asList
import org.w3c.dom.events.Event

// ------------------------------------------------------ dsl

public fun <T> RenderContext.tabs(
    store: TabStore<T> = TabStore(),
    box: Boolean = false,
    filled: Boolean = false,
    vertical: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    block: TabItemsBuilder<T>.() -> Unit = {}
): Tabs<T> {
    val tabs = register(Tabs(
        store,
        box = box,
        filled = filled,
        vertical = vertical,
        id = id,
        baseClass = baseClass,
        job
    ), {})
    val tabItems = TabItemsBuilder(tabs).apply(block).build()
    store.update(tabItems)
    return tabs
}

public fun <T> TabItemsBuilder<T>.tabItem(
    item: T,
    selected: Boolean = false,
    icon: (Span.() -> Unit)? = null,
    content: TabContent<T>.() -> Unit = {}
) {
    tabItems.add(TabItem(item, selected = selected, icon = icon, content = content))
}

// ------------------------------------------------------ tag

public class Tabs<T> internal constructor(
    public val store: TabStore<T>,
    public val box: Boolean,
    public val filled: Boolean,
    public val vertical: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, job = job) {

    private val scrollStore = ScrollButtonStore()
    private lateinit var tabs: Ul
    internal lateinit var tabDisplay: ComponentDisplay<Span, T>
    internal var contentDisplay: ComponentDisplay<TabContent<T>, T>? = null

    init {
        markAs(ComponentType.Tabs)

        div(baseClass = classes {
            +"tabs".component()
            +("box".modifier() `when` box)
            +("fill".modifier() `when` filled)
            +("vertical".modifier() `when` vertical)
            +baseClass
        }) {
            classMap(this@Tabs.scrollStore.data.map {
                mapOf("scrollable".modifier() to (!this@Tabs.vertical && it.showButtons))
            })
            button(baseClass = "tabs".component("scroll", "button")) {
                aria["label"] = "Scroll left"
                disabled(this@Tabs.scrollStore.data.map { it.disableLeft })
                aria["hidden"] = this@Tabs.scrollStore.data.map { it.disableLeft.toString() }
                domNode.onclick = { this@Tabs.scrollLeft(this@Tabs.tabs.domNode) }
                icon("angle-left".fas())
            }
            this@Tabs.tabs = ul(baseClass = "tabs".component("list")) {
                // update scroll buttons, when scroll event has been fired
                // e.g. by scrollLeft() or scrollRight()
                scrolls
                    .map { this@Tabs.updateScrollButtons(domNode) }
                    .filterNotNull()
                    .handledBy(this@Tabs.scrollStore.update)

                this@Tabs.store.data.renderEach({ this@Tabs.selectId(it) }, { tab ->
                    li(baseClass = classes {
                        +"tabs".component("item")
                        +("current".modifier() `when` tab.selected)
                    }) {
                        button(id = this@Tabs.tabId(tab.item), baseClass = "tabs".component("link")) {
                            aria["controls"] = this@Tabs.contentId(tab.item)
                            clicks.map { tab } handledBy this@Tabs.store.select
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
                })
            }
            button(baseClass = "tabs".component("scroll", "button")) {
                aria["label"] = "Scroll right"
                disabled(this@Tabs.scrollStore.data.map { it.disableRight })
                aria["hidden"] = this@Tabs.scrollStore.data.map { it.disableRight.toString() }
                domNode.onclick = { this@Tabs.scrollRight(this@Tabs.tabs.domNode) }
                icon("angle-right".fas())
            }
        }

        store.data.renderShifted(
            1,
            this,
            { selectId(it) },
            { tab ->
                register(TabContent(tab.item, tabId(tab.item), contentId(tab.item), job), {
                    if (!tab.selected) {
                        it.attr("hidden", "")
                    }
                    contentDisplay?.invoke(it, it.item)
                    tab.content(it)
                })
            }
        )

        // update scroll buttons, when tab items have been updated
        store.data.map { updateScrollButtons(tabs.domNode) }.filterNotNull() handledBy scrollStore.update

        // update scroll buttons, when window has been resized
        callbackFlow {
            val listener: (Event) -> Unit = { offer(it) }
            window.addEventListener(Events.resize.name, listener)
            awaitClose { domNode.removeEventListener(Events.resize.name, listener) }
        }.map { updateScrollButtons(tabs.domNode) }.filterNotNull() handledBy scrollStore.update
    }

    private fun selectId(tabItem: TabItem<T>) = Id.build(store.identifier(tabItem.item), tabItem.selected.toString())
    private fun tabId(item: T) = Id.build(store.identifier(item), "tab")
    private fun contentId(item: T) = Id.build(store.identifier(item), "cnt")

    private fun updateScrollButtons(tabs: HTMLUListElement): ScrollButton? {
        val first = tabs.firstElementChild
        val last = tabs.lastElementChild
        return if (first != null && last != null) {
            val overflowOnLeft = !first.isInView(tabs)
            val overflowOnRight = !last.isInView(tabs)
            val showButtons = overflowOnLeft || overflowOnRight
            val disableLeft = !overflowOnLeft
            val disableRight = !overflowOnRight
            ScrollButton(showButtons, disableLeft, disableRight)
        } else null
    }

    // find first Element that is fully in view on the left, then scroll to the element before it
    private fun scrollLeft(tabs: HTMLUListElement) {
        var firstElementInView: HTMLElement? = null
        var lastElementOutOfView: HTMLElement? = null
        val iterator = tabs.childNodes.asList().filterIsInstance<HTMLElement>().listIterator()

        while (iterator.hasNext() && firstElementInView == null) {
            val child = iterator.next()
            if (child.isInView(tabs)) {
                firstElementInView = child
                if (iterator.hasPrevious()) {
                    lastElementOutOfView = iterator.previous()
                }
            }
        }
        if (lastElementOutOfView != null) {
            tabs.scrollLeft -= lastElementOutOfView.scrollWidth
        }
    }

    // find last Element that is fully in view on the right, then scroll to the element after it
    private fun scrollRight(tabs: HTMLUListElement) {
        var lastElementInView: HTMLElement? = null
        var firstElementOutOfView: HTMLElement? = null
        val elements = tabs.childNodes.asList().filterIsInstance<HTMLElement>()
        val iterator = elements.listIterator(elements.size)

        while (iterator.hasPrevious() && lastElementInView == null) {
            val child = iterator.previous()
            if (child.isInView(tabs)) {
                lastElementInView = child
                if (iterator.hasNext()) {
                    firstElementOutOfView = iterator.next()
                }
            }
        }
        if (firstElementOutOfView != null) {
            tabs.scrollLeft += firstElementOutOfView.scrollWidth
        }
    }
}

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

internal class ScrollButtonStore : RootStore<ScrollButton>(ScrollButton()) {

    val showButtons: SimpleHandler<Boolean> = handle { scrollButton, show ->
        scrollButton.copy(showButtons = show)
    }

    val disableLeft: SimpleHandler<Boolean> = handle { scrollButton, disable ->
        scrollButton.copy(disableLeft = disable)
    }

    val disableRight: SimpleHandler<Boolean> = handle { scrollButton, disable ->
        scrollButton.copy(disableRight = disable)
    }
}

public class TabStore<T>(public val identifier: IdProvider<T, String> = { Id.build(it.toString()) }) :
    RootStore<List<TabItem<T>>>(emptyList()) {

    public val select: EmittingHandler<TabItem<T>, TabItem<T>> = handleAndEmit { items, tab ->
        emit(tab)
        items.map { if (identifier(it.item) == identifier(tab.item)) it.select() else it.unselect() }
    }
}

// ------------------------------------------------------ types

public data class ScrollButton(
    val showButtons: Boolean = false,
    val disableLeft: Boolean = true,
    val disableRight: Boolean = false
)

public class TabItem<T>(
    override val item: T,
    public val selected: Boolean = false,
    internal val icon: (Span.() -> Unit)? = null,
    internal val content: TabContent<T>.() -> Unit = {}
) : HasItem<T> {
    internal fun select(): TabItem<T> = TabItem(item, true, icon, content)
    internal fun unselect(): TabItem<T> = TabItem(item, false, icon, content)
}

public class TabItemsBuilder<T> internal constructor(private val tabs: Tabs<T>) {
    internal val tabItems: MutableList<TabItem<T>> = mutableListOf()

    public var itemDisplay: ComponentDisplay<Span, T> = { +it.toString() }

    public var contentDisplay: ComponentDisplay<TabContent<T>, T>? = null

    internal fun build(): List<TabItem<T>> {
        tabs.tabDisplay = itemDisplay
        tabs.contentDisplay = contentDisplay
        return when (tabItems.count { it.selected }) {
            0 -> {
                // no selection -> select first
                tabItems.mapIndexed { index, tab ->
                    if (index == 0) tab.select() else tab
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
                            modified.add(tabItem.unselect())
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
