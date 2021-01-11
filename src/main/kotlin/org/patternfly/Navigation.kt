package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.lenses.IdProvider
import dev.fritz2.routing.Router
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.dom.clear
import org.patternfly.DividerVariant.HR
import org.patternfly.DividerVariant.LI
import org.patternfly.Orientation.HORIZONTAL
import org.patternfly.Orientation.VERTICAL
import org.patternfly.Settings.UI_TIMEOUT
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.debug
import org.patternfly.dom.querySelector
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.events.Event

// ------------------------------------------------------ dsl

/**
 * Creates a horizontal [Navigation] component.
 *
 * @param router the router instance
 * @param store the store for the navigation items
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.NavigationSample.horizontal
 */
public fun <T> RenderContext.horizontalNavigation(
    router: Router<T>,
    store: NavigationStore<T> = NavigationStore(),
    id: String? = null,
    baseClass: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(
    Navigation(
        router = router,
        store = store,
        orientation = HORIZONTAL,
        expandable = false,
        tertiary = false,
        id = id,
        baseClass = baseClass,
        job = job,
    ),
    content
)

/**
 * Creates a tertiary [Navigation] component.
 *
 * @param router the router instance
 * @param store the store for the navigation items
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.NavigationSample.tertiary
 */
public fun <T> RenderContext.tertiaryNavigation(
    router: Router<T>,
    store: NavigationStore<T> = NavigationStore(),
    id: String? = null,
    baseClass: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(
    Navigation(
        router = router,
        store = store,
        orientation = HORIZONTAL,
        expandable = false,
        tertiary = true,
        id = id,
        baseClass = baseClass,
        job = job,
    ),
    content
)

/**
 * Creates a vertical [Navigation] component.
 *
 * @param router the router instance
 * @param store the store for the navigation items
 * @param expandable whether the navigation uses expandable groups
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.NavigationSample.vertical
 */
public fun <T> RenderContext.verticalNavigation(
    router: Router<T>,
    store: NavigationStore<T> = NavigationStore(),
    expandable: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(
    Navigation(
        router = router,
        store = store,
        orientation = VERTICAL,
        expandable = expandable,
        tertiary = false,
        id = id,
        baseClass = baseClass,
        job = job
    ),
    content
)

/**
 * Starts a block to add navigation items using the DSL.
 *
 * @param block code block for adding the navigation items.
 */
public fun <T> Navigation<T>.items(block: ItemsBuilder<T>.() -> Unit = {}) {
    val entries = ItemsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

/**
 * Starts a block to add navigation groups using the DSL.
 *
 * @param block code block for adding the navigation groups.
 */
public fun <T> Navigation<T>.groups(block: GroupsBuilder<T>.() -> Unit = {}) {
    val entries = GroupsBuilder(store.idProvider, store.itemSelection).apply(block).build()
    store.update(entries)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [navigation](https://www.patternfly.org/v4/components/navigation/design-guidelines) component.
 *
 * A navigation organizes an application’s structure and content, making it easy to find information and accomplish tasks. Navigation communicates relationships, context, and actions a user can take within an application.
 *
 * The navigation component comes in three different variations:
 *
 * ### Horizontal navigation
 *
 * Horizontal navigation is global navigation that displays navigation items from left to right in the [Header].
 *
 * ### Vertical navigation
 *
 * Vertical navigation is hierarchical global navigation that displays navigation options from top to bottom on the left side of a screen. Vertical navigation can be collapsed to provide additional screen real estate by using a menu icon button at the top left.
 *
 * Items in a vertical navigation can be flat or grouped. When groups are used the value of `expandable` matters:
 *
 * - `expandable == true`: Items in a group without a text are rendered as flat items before all other groups. Items in groups with a text are rendered as expandable groups.
 * - `expandable == false`: All groups should have a text and are treated equally.
 *
 * ### Tertiary navigation
 *
 * While global navigation controls what users are seeing at the application-level, local navigation provides more granular navigation specific to a particular page or window in the application. For example, a user might use global navigation to get to a settings page, and then use local navigation to access privacy and general user settings.
 *
 * ### Entries
 *
 * The data in the navigation component is managed by a [NavigationStore] and is wrapped inside instances of [Item]. The type of the [Router] must match the type of the [Item]s.
 *
 * #### Adding entries
 *
 * Entries can be added by using the [NavigationStore] or by using the DSL. Horizontal and tertiary navigation supports only flat items while vertical navigation also supports groups. Nested groups are not supported.
 *
 * #### Rendering entries
 *
 * By default the navigation uses [Group.text] and [Item.text] to render the data in the [NavigationStore]. If you don't want to use the builtin defaults you can specify a custom display function by calling [display].
 *
 * [Item.icon], [Item.description], [Item.href] and [Item.disabled] are ignored by the navigation component.
 *
 * ### Routing
 *
 * The passed [Router] is used for the actual navigation (updates the location & history). As mentioned above the type of the [Router] must match the type of the [Item]s.
 *
 * @sample org.patternfly.sample.NavigationSample.expandable
 */
@Suppress("LongParameterList")
public class Navigation<T> internal constructor(
    internal val router: Router<T>,
    internal val store: NavigationStore<T>,
    private val orientation: Orientation,
    expandable: Boolean,
    tertiary: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLElement>,
    TextElement(
        "nav",
        id = id,
        baseClass = classes {
            +ComponentType.Navigation
            +("horizontal".modifier() `when` (orientation == HORIZONTAL))
            +("tertiary".modifier() `when` tertiary)
            +baseClass
        },
        job
    ) {

    private lateinit var ul: Ul
    private val scrollStore = ScrollButtonStore()
    internal var display: ComponentDisplay<A, T>? = null

    init {
        markAs(ComponentType.Navigation)
        if (!tertiary) {
            aria["label"] = "Global"
        }

        (MainScope() + job).launch {
            store.data.collect { entries ->
                domNode.clear()

                if (orientation == HORIZONTAL) {
                    horizontal(entries)
                } else {
                    if (entries.groups.isEmpty()) {
                        flat(entries, false)
                    } else {
                        if (expandable) {
                            verticalExpandable(entries)
                        } else {
                            verticalGrouped(entries)
                        }
                    }
                }
            }
        }

        if (orientation == HORIZONTAL) {
            classMap(
                scrollStore.data.map {
                    mapOf("scrollable".modifier() to (it.showButtons))
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
    }

    private fun RenderContext.horizontal(entries: Entries<T>) {
        button(baseClass = "nav".component("scroll", "button")) {
            aria["label"] = "Scroll left"
            disabled(this@Navigation.scrollStore.data.map { it.disableLeft })
            aria["hidden"] = this@Navigation.scrollStore.data.map { it.disableLeft.toString() }
            domNode.onclick = { this@Navigation.ul.domNode.scrollLeft() }
            icon("angle-left".fas())
        }
        flat(entries, true)
        button(baseClass = "nav".component("scroll", "button")) {
            aria["label"] = "Scroll right"
            disabled(this@Navigation.scrollStore.data.map { it.disableRight })
            aria["hidden"] = this@Navigation.scrollStore.data.map { it.disableRight.toString() }
            domNode.onclick = { this@Navigation.ul.domNode.scrollRight() }
            icon("angle-right".fas())
        }
    }

    private fun RenderContext.flat(entries: Entries<T>, scroll: Boolean) {
        ul = ul(baseClass = "nav".component("list")) {
            if (scroll) {
                // update scroll buttons, when scroll event has been fired
                // e.g. by scrollLeft() or scrollRight()
                scrolls.map { domNode.updateScrollButtons() }
                    .filterNotNull()
                    .handledBy(this@Navigation.scrollStore.update)
            }

            entries.entries.forEach { entry ->
                when (entry) {
                    is Group -> {
                        li(baseClass = "display-none".util()) {
                            attr("hidden", true)
                            val message = buildString {
                                append("Groups are not supported for ")
                                if (this@Navigation.orientation == HORIZONTAL) {
                                    append("horizontal")
                                } else {
                                    append("flat vertical")
                                }
                                append(" navigation")
                            }
                            !message
                            console.warn("$message: ${domNode.debug()}")
                        }
                    }
                    is Item -> {
                        renderLink(this@Navigation, entry)
                    }
                    is Separator -> {
                        divider(LI)
                    }
                }
            }
        }
    }

    private fun RenderContext.verticalGrouped(entries: Entries<T>) {
        entries.entries.forEach { entry ->
            when (entry) {
                is Group<T> -> {
                    val headerId = Id.unique(ComponentType.Navigation.id, "grp")
                    section(baseClass = "nav".component("section")) {
                        aria["lebelledby"] = headerId
                        h2("nav".component("section", "title"), headerId) {
                            +(entry.text ?: "n/a")
                        }
                        renderGroupEntries(this@Navigation, entry)
                    }
                }
                is Item<T> -> {
                    section(baseClass = "display-none".util()) {
                        attr("hidden", true)
                        val message = "Flat items are not supported for grouped vertical navigation"
                        !message
                        console.warn("$message: ${domNode.debug()}")
                    }
                }
                is Separator<T> -> {
                    divider(HR)
                }
            }
        }
    }

    private fun RenderContext.verticalExpandable(entries: Entries<T>) {
        ul(baseClass = "nav".component("list")) {
            // turn entries.all into a list containing
            // 1) the items and separators of all unnamed groups and
            // 2) all other entries
            val (unnamedGroups, allOtherEntries) =
                entries.all.partition { it is Group<T> && it.text == null }
            console.log("entries partitions")
            val itemsAndSeparatorsOfUnnamedGroups = unnamedGroups.flatMap { entry ->
                when (entry) {
                    is Group<T> -> entry.entries.filter { it is Item<T> || it is Separator<T> }
                    else -> listOf(entry)
                }
            }
            entries.copy(all = itemsAndSeparatorsOfUnnamedGroups + allOtherEntries).entries.forEach { entry ->
                when (entry) {
                    is Group -> {
                        register(ExpandableGroup(this@Navigation, entry, job), {})
                    }
                    is Item -> {
                        renderLink(this@Navigation, entry)
                    }
                    is Separator -> {
                        divider(LI)
                    }
                }
            }
        }
    }

    /**
     * Sets a custom display function to render the navigation items.
     */
    public fun display(display: ComponentDisplay<A, T>) {
        this.display = display
    }
}

internal class ExpandableGroup<T>(
    private val navigation: Navigation<T>,
    private val group: Group<T>,
    job: Job
) : Tag<HTMLLIElement>(
    "li",
    baseClass = classes("nav".component("item"), "expandable".modifier()),
    job = job
) {

    private val expanded = ExpandedStore()

    init {
        // don't use classMap for expanded flow
        // classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
        (MainScope() + job).launch {
            expanded.data.collect { domNode.classList.toggle("expanded".modifier(), it) }
        }
        // it might interfere with router flow, which also modifies the class list
        (MainScope() + job).launch {
            this@ExpandableGroup.navigation.router.data.collect {
                delay(UI_TIMEOUT) // wait a little bit before testing for the current modifier
                val selector = By.classname("nav".component("link"), "current".modifier())
                val containsCurrent = domNode.querySelector(selector) != null
                domNode.classList.toggle("current".modifier(), containsCurrent)
            }
        }
        val buttonId = Id.unique(ComponentType.Navigation.id, "eg")
        button("nav".component("link"), buttonId) {
            +(this@ExpandableGroup.group.text ?: "n/a")
            clicks handledBy this@ExpandableGroup.expanded.toggle
            aria["expanded"] = this@ExpandableGroup.expanded.data.map { it.toString() }

            span("nav".component("toggle")) {
                span("nav".component("toggle", "icon")) {
                    icon("angle-right".fas())
                }
            }
        }
        section("nav".component("subnav")) {
            aria["labelledby"] = buttonId
            attr("hidden", this@ExpandableGroup.expanded.data.map { !it })
            renderGroupEntries(this@ExpandableGroup.navigation, this@ExpandableGroup.group)
        }
    }
}

private fun <T> RenderContext.renderGroupEntries(navigation: Navigation<T>, group: Group<T>) =
    ul(baseClass = "nav".component("list")) {
        group.entries.forEach { entry ->
            when (entry) {
                is Group -> {
                    console.warn(
                        "Nested groups are not supported for vertical grouped navigation: " +
                            navigation.domNode.debug()
                    )
                }
                is Item -> {
                    renderLink(navigation, entry)
                }
                is Separator -> {
                    divider(LI)
                }
            }
        }
    }

private fun <T> RenderContext.renderLink(navigation: Navigation<T>, item: Item<T>) =
    li(baseClass = "nav".component("item")) {
        a("nav".component("link")) {
            clicks.map { item.unwrap() } handledBy navigation.router.navTo
            classMap(
                navigation.router.data.map { route ->
                    mapOf("current".modifier() to (route == item.unwrap()))
                }
            )
            aria["current"] = navigation.router.data.map { route ->
                if (route == item.unwrap()) "page" else ""
            }
            if (navigation.display != null) {
                navigation.display?.invoke(this, item.unwrap())
            } else {
                +(item.text ?: "n/a")
            }
        }
    }

// ------------------------------------------------------ store

/**
 * An [EntriesStore] with [ItemSelection.SINGLE] selection mode.
 */
public class NavigationStore<T>(idProvider: IdProvider<T, String> = { Id.build(it.toString()) }) :
    EntriesStore<T>(idProvider, ItemSelection.SINGLE)
