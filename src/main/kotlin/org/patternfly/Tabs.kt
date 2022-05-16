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
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates a [Tabs] component.
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
    context: Tabs.() -> Unit = {}
) {
    Tabs(box = box, filled = filled, vertical = vertical).apply(context).render(this, baseClass, id)
}

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
public open class Tabs(
    private val box: Boolean,
    private val filled: Boolean,
    private val vertical: Boolean,
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private lateinit var ul: Ul
    private var itemsInStore: Boolean = false

    private val headItems: MutableList<TabItem> = mutableListOf()
    private val tailItems: MutableList<TabItem> = mutableListOf()

    private val scrollStore = ScrollButtonStore()
    private val itemStore: TabItemStore = TabItemStore()
    private val idSelection: RootStore<String?> = storeOf(null)
    private val disabledIds = object : RootStore<List<String>>(listOf()) {
        val disable: Handler<String> = handle { ids, id -> ids + id }
        val enable: Handler<String> = handle { ids, id -> ids - id }
    }

    /**
     * Adds a static [TabItem].
     *
     * @param id a unique id for the tab item
     * @param context a lambda expression for setting up the tab item
     */
    public fun item(
        id: String = Id.unique(ComponentType.Tabs.id, "itm"),
        context: StaticTabItem.() -> Unit = {}
    ) {
        val item = StaticTabItem(id).apply(context)
        (if (itemsInStore) tailItems else headItems).add(item)

        item.selectedFlag?.let { selected ->
            if (selected) {
                idSelection.update(id)
            }
        }
        item.disabledFlag?.let { disabled ->
            if (disabled) {
                disabledIds.disable(id)
            }
        }
        item.selectedFlow?.let { selected ->
            // setup one-way selection data binding: flow -> id
            selected.filter { it }.map { id } handledBy idSelection.update
            selected.filter { !it }.map { null } handledBy idSelection.update
        }
        item.disabledFlow?.let { disabled ->
            // setup disabled one-way disabled data binding: flow -> id
            disabled.filter { it }.map { id } handledBy disabledIds.disable
            disabled.filter { !it }.map { id } handledBy disabledIds.enable
        }
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
                // 1. id -> T
                idSelection.data.map { idToData[it] } handledBy selection.update
                // 2. T -> id
                selection.data.map { if (it != null) idProvider(it) else null } handledBy idSelection.update

                // setup disabled two-way data bindings
                // id -> T
                disabledIds.data.map { ids ->
                    idToData.filterKeys { it in ids }
                }.map { it.values.toList() } handledBy disabled.update
                // T -> id
                disabled.data.map { data -> data.map { idProvider(it) } } handledBy disabledIds.update

                // update scroll buttons
                ul.domNode.updateScrollButtons()?.let { scrollStore.update(it) }
            }
        }
        itemsInStore = true
    }

    @Suppress("LongMethod")
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

            itemStore.data.map { items ->
                headItems + items + tailItems
            }.renderEach(idProvider = { it.id }) { item ->
                renderContent(this, item)
            }

            // update scroll buttons, when window has been resized
            ScrollButton.windowResizes().map { ul.domNode.updateScrollButtons() }
                .filterNotNull() handledBy scrollStore.update
            // initial update
            (MainScope() + job).launch {
                ul.domNode.updateScrollButtons()?.let { scrollStore.update(it) }
            }
        }
    }

    private fun renderItem(context: RenderContext, item: TabItem): Li = with(context) {
        li(baseClass = classes("tabs".component("item"))) {
            attr("role", "presentation")
            classMap(idSelection.data.filterNotNull().map { mapOf("current".modifier() to (it == item.id)) })
            button(id = item.tabId, baseClass = "tabs".component("link")) {
                aria["controls"] = item.contentId
                aria["selected"] = idSelection.data.map { (item.id == it).toString() }
                aria["disabled"] = disabledIds.data.map { (it.contains(item.id)).toString() }
                disabled(disabledIds.data.map { it.contains(item.id) })
                attr("role", "tab")
                clicks.map { item.id } handledBy idSelection.update

                item.tab?.let { tab ->
                    if (tab.iconFirst) {
                        renderIcon(this, tab)
                        renderText(this, tab)
                    } else {
                        renderText(this, tab)
                        renderIcon(this, tab)
                    }
                }
            }
        }
    }

    private fun renderIcon(context: RenderContext, tab: Tab) {
        with(context) {
            tab.icon?.let { icn ->
                span(baseClass = "tabs".component("item", "icon")) {
                    icn(this)
                }
            }
        }
    }

    private fun renderText(context: RenderContext, tab: Tab) {
        with(context) {
            if (tab.hasTitle) {
                span(baseClass = "tabs".component("item", "text")) {
                    tab.applyTitle(this)
                }
            }
        }
    }

    private fun renderContent(context: RenderContext, item: TabItem): TextElement = with(context) {
        section(baseClass = "tab-content".component(), id = item.contentId) {
            aria["labelledby"] = item.tabId
            attr("hidden", idSelection.data.map { item.id != it })
            attr("role", "tabpanel")
            attr("tabindex", 0)
            div(baseClass = "tab-content".component("body")) {
                item.content?.invoke(this)
            }
        }
    }
}

// ------------------------------------------------------ item & store

/**
 * DSL scope class to create [Tab]s when using [Tabs.items] functions.
 */
public class TabItemScope internal constructor(internal var id: String) {

    /**
     * Creates and returns a new [Tab].
     */
    public fun item(context: TabItem.() -> Unit = {}): TabItem = TabItem(id).apply(context)
}

/**
 * An item in a [Tabs] component. An item consists of the tab and the related content.
 */
public open class TabItem internal constructor(internal val id: String) {

    internal val tabId = Id.build(id, "tab")
    internal val contentId = Id.build(id, "cnt")
    internal var tab: Tab? = null
    internal var content: (Div.() -> Unit)? = null

    public fun tab(title: String? = null, tab: Tab.() -> Unit = {}) {
        this.tab = Tab(title).apply(tab)
    }

    public fun content(content: Div.() -> Unit) {
        this.content = content
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as TabItem

        if (id != other.id) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "TabItem(id='$id', tabId='$tabId', contentId='$contentId')"
    }
}

/**
 * A static item in a [Tabs] component.
 */
public class StaticTabItem internal constructor(id: String) : TabItem(id) {

    internal var selectedFlag: Boolean? = null
    internal var disabledFlag: Boolean? = null
    internal var selectedFlow: Flow<Boolean>? = null
    internal var disabledFlow: Flow<Boolean>? = null

    public fun selected(value: Boolean) {
        this.selectedFlag = value
    }

    public fun selected(value: Flow<Boolean>) {
        this.selectedFlow = value
    }

    public fun disabled(value: Boolean) {
        this.disabledFlag = value
    }

    public fun disabled(value: Flow<Boolean>) {
        this.disabledFlow = value
    }
}

public class Tab(title: String?) : WithTitle by TitleMixin() {

    internal var icon: (RenderContext.() -> Unit)? = null
    internal var iconFirst: Boolean = false

    init {
        title?.let { this.title(it) }
    }

    /**
     * Sets the render function for the icon of the tab.
     */
    public fun icon(iconClass: String = "", context: Icon.() -> Unit = {}) {
        iconFirst = !hasTitle
        this.icon = {
            icon(iconClass = iconClass) {
                context(this)
            }
        }
    }
}

internal class TabItemStore : RootStore<List<TabItem>>(emptyList())
