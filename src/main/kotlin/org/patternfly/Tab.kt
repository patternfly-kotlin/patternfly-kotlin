package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.handledBy
import dev.fritz2.lenses.IdProvider
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.Id
import org.patternfly.dom.hidden

// ------------------------------------------------------ factory

/**
 * Creates a [Tab] component.
 *
 * @param box whether tabs are outlined by a box.
 * @param filled whether tabs take all available space
 * @param vertical whether tabs are placed on the left-hand side of a page or container
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.tabs(
    box: Boolean = false,
    filled: Boolean = false,
    vertical: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Tab.() -> Unit = {}
) {
    Tab(box = box, filled = filled, vertical = vertical).apply(context).render(this, baseClass, id)
}

/**
 * Creates a tab content.
 *
 * @param forItem the tab item this content relates to
 * @param idProvider the id provider to identify the tab item
 * @param baseClass optional CSS class that should be applied to the component
 * @param content a lambda expression for setting up the tab content
 */
public fun <T> RenderContext.tabContent(
    forItem: T,
    idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
    baseClass: String? = null,
    content: Div.() -> Unit
): TextElement = tabContent(idProvider(forItem), baseClass, content)

/**
 * Creates a tab content.
 *
 * @param forTab the tab id this content relates to
 * @param baseClass optional CSS class that should be applied to the component
 * @param content a lambda expression for setting up the tab content
 */
public fun RenderContext.tabContent(
    forTab: String,
    baseClass: String? = null,
    content: Div.() -> Unit
): TextElement = section(baseClass = classes("tab-content".component(), baseClass), id = contentId(forTab)) {
    aria["labelledby"] = forTab
    attr("hidden", true)
    attr("role", "tabpanel")
    attr("tabindex", 0)
    div(baseClass = "tab-content".component("body")) {
        content(this)
    }
}

internal fun contentId(tabId: String) = Id.build(tabId, "cnt")

// ------------------------------------------------------ component

/**
 * PatternFly [tabs](https://www.patternfly.org/v4/components/tabs/design-guidelines) component.
 *
 * Tabs allow users to navigate between views within the same page or context.
 *
 * Tab items and tab content are created independently of each other.
 *
 * @sample org.patternfly.sample.TabsSample.tabs
 * @sample org.patternfly.sample.TabsSample.store
 */
public open class Tab(
    private val box: Boolean,
    private val filled: Boolean,
    private val vertical: Boolean,
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private lateinit var ul: Ul
    private val scrollStore = ScrollButtonStore()
    private var itemsInStore: Boolean = false
    private val itemStore: TabItemStore = TabItemStore()
    private val headItems: MutableList<TabItem> = mutableListOf()
    private val tailItems: MutableList<TabItem> = mutableListOf()
    private val idSelection: RootStore<String?> = storeOf(null)
    private val idDisabled = object : RootStore<List<String>>(listOf()) {
        val disable: Handler<String> = handle { ids, id -> ids + id }
    }

    /**
     * Adds a static [TabItem].
     *
     * @param id a unique id for the tab item
     * @param title the title of the tab item (can also be defined later)
     * @param selected whether the tab item is selected
     * @param disabled whether the tab item is disabled
     * @param context a lambda expression for setting up the tab item
     */
    public fun item(
        id: String,
        title: String? = null,
        selected: Boolean = false,
        disabled: Boolean = false,
        context: TabItem.() -> Unit = {}
    ) {
        if (selected) {
            idSelection.update(id)
        }
        if (disabled) {
            idDisabled.disable(id)
        }
        (if (itemsInStore) tailItems else headItems).add(TabItem(id, title).apply(context))
    }

    /**
     * Adds tab items from the specified values.
     *
     * @param values the values for the tab items
     * @param idProvider an id Provider to create unique IDs for the tabs
     * @param selection stores the selected tab item
     * @param disabled stores the disabled tab items
     * @param display function to render the tab item
     */
    public fun <T> items(
        values: Store<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        selection: Store<T?> = storeOf(null),
        disabled: Store<List<T>> = storeOf(listOf()),
        display: TabItemScope.(T) -> TabItem
    ) {
        items(values.data, idProvider, selection, disabled, display)
    }

    /**
     * Adds tab items from the specified values.
     *
     * @param values the values for the tab items
     * @param idProvider an id Provider to create unique IDs for the tabs
     * @param selection stores the selected tab item
     * @param disabled stores the disabled tab items
     * @param display function to render the tab item
     */
    public fun <T> items(
        values: Flow<List<T>>,
        idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
        selection: Store<T?> = storeOf(null),
        disabled: Store<List<T>> = storeOf(listOf()),
        display: TabItemScope.(T) -> TabItem
    ) {
        (MainScope() + itemStore.job).launch {
            values.collect { values ->
                val idToData = values.associateBy { idProvider(it) }
                itemStore.update(
                    values.map { value ->
                        TabItemScope(idProvider(value)).run {
                            display.invoke(this, value)
                        }
                    }
                )

                // setup selection two-way data bindings
                // 1. id -> data
                idSelection.data.map { idToData[it] } handledBy selection.update
                // 2. data -> id
                selection.data.map { if (it != null) idProvider(it) else null } handledBy idSelection.update

                // setup disabled two-way data bindings
                // id -> data
                idDisabled.data.map { ids ->
                    idToData.filterKeys { it in ids }
                }.map { it.values.toList() } handledBy disabled.update
                // data -> id
                disabled.data.map { data -> data.map { idProvider(it) } } handledBy idDisabled.update

                // update scroll buttons
                ul.domNode.updateScrollButtons()?.let { scrollStore.update(it) }
            }
        }
        itemsInStore = true
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +"tabs".component()
                    +("box".modifier() `when` box)
                    +("fill".modifier() `when` filled)
                    +("vertical".modifier() `when` vertical)
                    +baseClass
                }
            ) {
                markAs(ComponentType.Tabs)
                applyElement(this)
                applyEvents(this)

                classMap(
                    scrollStore.data.map {
                        mapOf("scrollable".modifier() to (!vertical && it.showButtons))
                    }
                )
                button(baseClass = "tabs".component("scroll", "button")) {
                    aria["label"] = "Scroll left"
                    disabled(scrollStore.data.map { it.disableLeft })
                    aria["hidden"] = scrollStore.data.map { it.disableLeft.toString() }
                    domNode.onclick = { ul.domNode.scrollLeft() }
                    icon("angle-left".fas())
                }
                ul = ul(baseClass = "tabs".component("list")) {
                    // update scroll buttons, when scroll event has been fired
                    // e.g. by scrollLeft() or scrollRight()
                    ScrollButton.scrolls(domNode).map {
                        domNode.updateScrollButtons()
                    }.filterNotNull() handledBy (scrollStore.update)

                    itemStore.data.map { items ->
                        headItems + items + tailItems
                    }.renderEach(idProvider = { it.id }, into = this) { item ->
                    renderItem(this, item)
                }
                }
                button(baseClass = "tabs".component("scroll", "button")) {
                    aria["label"] = "Scroll right"
                    disabled(scrollStore.data.map { it.disableRight })
                    aria["hidden"] = scrollStore.data.map { it.disableRight.toString() }
                    domNode.onclick = { ul.domNode.scrollRight() }
                    icon("angle-right".fas())
                }
            }

            // update scroll buttons, when window has been resized
            ScrollButton.windowResizes().map { ul.domNode.updateScrollButtons() }
                .filterNotNull() handledBy scrollStore.update
            // initial update
            (MainScope() + job).launch {
                ul.domNode.updateScrollButtons()?.let { scrollStore.update(it) }
            }

            // control tab content
            (MainScope() + job).launch {
                idSelection.data.filterNotNull().distinctUntilChanged().collect { selectedTabId ->
                    for (item in (headItems + itemStore.current + tailItems)) {
                        document.getElementById(contentId(item.id))?.hidden = item.id != selectedTabId
                    }
                }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: TabItem): Li = with(context) {
        li(baseClass = classes("tabs".component("item"))) {
            attr("role", "presentation")
            classMap(idSelection.data.filterNotNull().map { mapOf("current".modifier() to (it == item.id)) })
            button(id = item.id, baseClass = "tabs".component("link")) {
                aria["controls"] = contentId(item.id)
                aria["selected"] = idSelection.data.map { (item.id == it).toString() }
                aria["disabled"] = idDisabled.data.map { (it.contains(item.id)).toString() }
                disabled(idDisabled.data.map { it.contains(item.id) })
                attr("role", "tab")
                clicks.map { item.id } handledBy idSelection.update
                item.icon?.let { icn ->
                    span(baseClass = "tabs".component("item", "icon")) {
                        icn.invoke(this)
                    }
                }
                span(baseClass = "tabs".component("item", "text")) {
                    item.applyTitle(this)
                }
            }
        }
    }
}

// ------------------------------------------------------ item & store

/**
 * DSL scope class to create [TabItem]s when using [Tab.items] functions.
 */
public class TabItemScope internal constructor(internal var id: String) {

    /**
     * Creates and returns a new [TabItem].
     */
    public fun item(title: String? = null, context: TabItem.() -> Unit = {}): TabItem =
        TabItem(id, title).apply(context)
}

/**
 * An item in an [Tab] component.
 */
public class TabItem internal constructor(internal val id: String, title: String?) :
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal var icon: (RenderContext.() -> Unit)? = null

    init {
        title?.let { this.title(it) }
    }

    /**
     * Sets the render function for the icon of the tab.
     */
    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        this.icon = {
            icon(iconClass = iconClass) {
                context(this)
            }
        }
    }
}

internal class TabItemStore : RootStore<List<TabItem>>(emptyList())
