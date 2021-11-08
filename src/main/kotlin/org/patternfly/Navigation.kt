@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.patternfly

import dev.fritz2.binding.EmittingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.lenses.IdProvider
import dev.fritz2.routing.Router
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.NotificationStore.handledBy
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.debug
import org.patternfly.dom.querySelector
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

// ------------------------------------------------------ factory

/**
 * Creates an [Navigation] component.
 *
 * @param router the router instance
 * @param store the store used to manage [NavigationEntry]
 * @param expandable whether groups are expandable (only applies to vertical variant)
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param build a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.navigation(
    router: Router<T>,
    store: NavigationStore<T> = NavigationStore(),
    expandable: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    build: Navigation<T>.() -> Unit = {}
) {
    Navigation(router, store, expandable).apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [navigation](https://www.patternfly.org/v4/components/navigation/design-guidelines) component.
 *
 * A navigation organizes an application’s structure and content, making it easy to find information and accomplish tasks. Navigation communicates relationships, context, and actions a user can take within an application.
 *
 * The navigation component comes in three different variations:
 *
 * 1. Vertical navigation: Vertical navigation is hierarchical global navigation that displays navigation options from top to bottom on the [Page.sidebar]. PatternFly vertical navigation can be collapsed to provide additional screen real estate by using a menu icon button at the top left.
 * 1. Horizontal navigation: Horizontal navigation is global navigation that displays navigation items from left to right in the application's [Masthead].
 * 1. Secondary horizontal navigation: Use secondary horizontal navigation when you want to provide more granular navigation specific to a particular page or window in your application. Use [pageSubNav] as a container for the secondary horizontal navigation.
 *
 * The navigation component will detect which variant to use, based on the container it is added to:
 *
 * - if added to [pageSubNav], secondary horizontal navigation will be used
 * - if added to [Masthead], horizontal navigation will be used
 * - if added to [Page.sidebar], vertical navigation will be used
 *
 * ### Entries
 *
 * The entries of the navigation can be added directly using [item], [group] and [separator] methods or by using a store. See the samples for more details.
 *
 * ### Routing
 *
 * The passed [Router] is used for the actual navigation (updates the location & history).
 *
 * @sample org.patternfly.sample.NavigationSample.horizontal
 * @sample org.patternfly.sample.NavigationSample.horizontalSubNav
 * @sample org.patternfly.sample.NavigationSample.vertical
 * @sample org.patternfly.sample.NavigationSample.store
 */
@Suppress("TooManyFunctions")
public class Navigation<T> internal constructor(
    private val router: Router<T>,
    public val store: NavigationStore<T>,
    private var expandable: Boolean
) : PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<TextElement, HTMLElement> by ElementMixin(),
    WithEvents<HTMLElement> by EventMixin() {

    private lateinit var root: TextElement
    private lateinit var ul: Ul
    private val scrollStore = ScrollButtonStore()
    private val entries: MutableList<NavigationEntry<T>> = mutableListOf()

    init {
        store.handleClicks.map { item -> item.route } handledBy router.navTo
    }

    /**
     * Modifies the [expandable] flag.
     */
    public fun expandable(expandable: Boolean) {
        this.expandable = expandable
    }

    /**
     * Adds a group and items;
     */
    public fun group(title: String, context: NavigationGroup<T>.() -> Unit) {
        entries.add(NavigationGroup<T>(title).apply(context))
    }

    /**
     * Adds an item.
     */
    public fun item(route: T, title: String) {
        entries.add(NavigationItem(route, title))
    }

    /**
     * Adds a separator.
     */
    public fun separator() {
        entries.add(NavigationSeparator())
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            val variant = when {
                // Order is important! Do *not* rearrange!
                scope.contains(Scopes.PAGE_SUBNAV) -> NavigationVariant.SUBNAV
                scope.contains(Scopes.MASTHEAD) -> NavigationVariant.HORIZONTAL
                else -> NavigationVariant.VERTICAL
            }

            root = nav(
                baseClass = classes {
                    +ComponentType.Navigation
                    +variant.modifier
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Navigation)
                if (variant != NavigationVariant.SUBNAV) {
                    aria["label"] = "Global"
                }

                if (variant == NavigationVariant.HORIZONTAL || variant == NavigationVariant.SUBNAV) {
                    classMap(scrollStore.data.map { mapOf("scrollable".modifier() to (it.showButtons)) })
                    // update scroll buttons, when navigation entries have been updated
                    store.data.map { ul.domNode.updateScrollButtons() }.filterNotNull() handledBy scrollStore.update
                    // update scroll buttons, when window has been resized
                    callbackFlow {
                        val listener: (Event) -> Unit = { this.trySend(it).isSuccess }
                        window.addEventListener(Events.resize.name, listener)
                        awaitClose { domNode.removeEventListener(Events.resize.name, listener) }
                    }.map {
                        ul.domNode.updateScrollButtons()
                    }.filterNotNull() handledBy scrollStore.update
                }

                store.data.render { entries ->
                    if (variant == NavigationVariant.HORIZONTAL || variant == NavigationVariant.SUBNAV) {
                        horizontal(this, entries)
                    } else {
                        if (entries.filterIsInstance<NavigationGroup<T>>().isEmpty()) {
                            flat(this, entries, false)
                        } else {
                            if (expandable) {
                                verticalExpandable(this, entries)
                            } else {
                                verticalGrouped(this, entries)
                            }
                        }
                    }
                }
            }
        }

        if (entries.isNotEmpty()) {
            store.update(entries)
        }
    }

    private fun horizontal(context: RenderContext, entries: List<NavigationEntry<T>>) {
        with(context) {
            button(baseClass = "nav".component("scroll", "button")) {
                aria["label"] = "Scroll left"
                disabled(scrollStore.data.map { it.disableLeft })
                aria["hidden"] = scrollStore.data.map { it.disableLeft.toString() }
                domNode.onclick = { ul.domNode.scrollLeft() }
                icon("angle-left".fas())
            }
            flat(this, entries, true)
            button(baseClass = "nav".component("scroll", "button")) {
                aria["label"] = "Scroll right"
                disabled(scrollStore.data.map { it.disableRight })
                aria["hidden"] = scrollStore.data.map { it.disableRight.toString() }
                domNode.onclick = { ul.domNode.scrollRight() }
                icon("angle-right".fas())
            }
        }
    }

    private fun verticalGrouped(context: RenderContext, entries: List<NavigationEntry<T>>) {
        with(context) {
            entries.forEach { entry ->
                when (entry) {
                    is NavigationGroup<T> -> {
                        val headerId = Id.unique(ComponentType.Navigation.id, "grp")
                        section(baseClass = "nav".component("section")) {
                            aria["lebelledby"] = headerId
                            h2("nav".component("section", "title"), headerId) {
                                +entry.title
                            }
                            group(this, entry)
                        }
                    }
                    is NavigationItem<T> -> warning(
                        this,
                        "Flat items are not supported for vertical grouped navigation"
                    )
                    is NavigationSeparator<T> -> divider(DividerVariant.HR)
                }
            }
        }
    }

    private fun verticalExpandable(context: RenderContext, entries: List<NavigationEntry<T>>) {
        with(context) {
            ul(baseClass = "nav".component("list")) {
                entries.forEach { entry ->
                    when (entry) {
                        is NavigationGroup -> expandableGroup(this, entry)
                        is NavigationItem -> link(this, entry)
                        is NavigationSeparator -> divider(DividerVariant.LI)
                    }
                }
            }
        }
    }

    private fun flat(context: RenderContext, entries: List<NavigationEntry<T>>, scroll: Boolean) {
        with(context) {
            ul = ul(baseClass = "nav".component("list")) {
                if (scroll) {
                    // update scroll buttons, when scroll event has been fired
                    // e.g. by scrollLeft() or scrollRight()
                    // Using scrolls.map leads to a CCE :-(
                    callbackFlow {
                        val listener: (Event) -> Unit = { this.trySend(it).isSuccess }
                        domNode.addEventListener(Events.scroll.name, listener)
                        awaitClose { domNode.removeEventListener(Events.scroll.name, listener) }
                    }.map { domNode.updateScrollButtons() }
                        .filterNotNull()
                        .handledBy(scrollStore.update)
                }
                entries.forEach { entry ->
                    when (entry) {
                        is NavigationGroup<T> -> warning(
                            this,
                            buildString {
                                append("Groups are not supported in ")
                                append(if (scroll) "horizontal" else "flat vertical")
                                append(" navigation")
                            }
                        )
                        is NavigationItem<T> -> link(this, entry)
                        is NavigationSeparator<T> -> divider(DividerVariant.LI)
                    }
                }
            }
        }
    }

    private fun group(context: RenderContext, group: NavigationGroup<T>) {
        with(context) {
            ul(baseClass = "nav".component("list")) {
                group.mutableEntries.forEach { entry ->
                    when (entry) {
                        is NavigationGroup<T> -> warning(
                            this,
                            "Nested groups are not supported in vertical navigation"
                        )
                        is NavigationItem<T> -> link(this, entry)
                        is NavigationSeparator<T> -> divider(DividerVariant.LI)
                    }
                }
            }
        }
    }

    private fun expandableGroup(context: RenderContext, group: NavigationGroup<T>) {
        with(context) {
            li(baseClass = classes("nav".component("item"), "expandable".modifier())) {
                (MainScope() + job).launch {
                    group.expanded.data.collect { domNode.classList.toggle("expanded".modifier(), it) }
                }
                (MainScope() + job).launch {
                    router.data.collect {
                        delay(Settings.UI_TIMEOUT) // wait a bit before testing for the current modifier
                        val selector = By.classname("nav".component("link"), "current".modifier())
                        val containsCurrent = domNode.querySelector(selector) != null
                        domNode.classList.toggle("current".modifier(), containsCurrent)
                    }
                }
                val buttonId = Id.unique(ComponentType.Navigation.id, "eg")
                button("nav".component("link"), buttonId) {
                    +group.title
                    clicks handledBy group.expanded.toggle
                    aria["expanded"] = group.expanded.data.map { it.toString() }
                    span("nav".component("toggle")) {
                        span("nav".component("toggle", "icon")) {
                            icon("angle-right".fas())
                        }
                    }
                }
                section("nav".component("subnav")) {
                    aria["labelledby"] = buttonId
                    attr("hidden", group.expanded.data.map { !it })
                    group(this, group)
                }
            }
        }
    }

    private fun link(context: RenderContext, item: NavigationItem<T>) {
        with(context) {
            li(baseClass = "nav".component("item")) {
                a("nav".component("link")) {
                    clicks.map { item } handledBy store.handleClicks
                    classMap(
                        router.data.map { route ->
                            mapOf("current".modifier() to (route == item.route))
                        }
                    )
                    aria["current"] = router.data.map { route ->
                        if (route == item.route) "page" else ""
                    }
                    +item.title
                }
            }
        }
    }

    private fun warning(context: RenderContext, message: String) {
        with(context) {
            !message
            console.warn("$message: ${root.domNode.debug()}")
        }
    }
}

internal enum class NavigationVariant(val modifier: String?) {
    VERTICAL(null),
    HORIZONTAL("horizontal".modifier()),
    SUBNAV("horizontal-subnav".modifier())
}

// ------------------------------------------------------ navigation entry

/**
 * Base class for groups and items.
 */
public sealed class NavigationEntry<T>

/**
 * A navigation group with a title and nested items.
 *
 * Please note that nested groups are *not* supported!
 */
public class NavigationGroup<T>(public val title: String, initialEntries: List<NavigationEntry<T>> = emptyList()) :
    NavigationEntry<T>(),
    Expandable by ExpandedMixin() {

    internal val id: String = Id.unique(ComponentType.Navigation.id, "grp")
    internal val mutableEntries: MutableList<NavigationEntry<T>> = mutableListOf()
    public val entries: List<NavigationEntry<T>> get() = mutableEntries

    init {
        mutableEntries.addAll(initialEntries)
    }

    /**
     * Adds an item to this group.
     */
    public fun item(route: T, title: String) {
        mutableEntries.add(NavigationItem(route, title).apply { group = this@NavigationGroup })
    }

    /**
     * Adds a separator to this group.
     */
    public fun separator() {
        mutableEntries.add(NavigationSeparator())
    }
}

/**
 * A navigation item with a route and a title.
 */
public class NavigationItem<T>(public val route: T, public val title: String) :
    NavigationEntry<T>() {

    internal val id: String = Id.unique(ComponentType.Navigation.id, "itm")
    internal var group: NavigationGroup<T>? = null
}

/**
 * A navigation separator.
 */
public class NavigationSeparator<T> : NavigationEntry<T>()

// ------------------------------------------------------ store

public class NavigationStore<T>(
    initialData: List<NavigationEntry<T>> = emptyList(),
    override val idProvider: IdProvider<T, String> = { Id.build(it.toString()) }
) : RootStore<List<NavigationEntry<T>>>(initialData), WithIdProvider<T> {

    internal val handleClicks: EmittingHandler<NavigationItem<T>, NavigationItem<T>> =
        handleAndEmit { entries, item ->
            entries
                .map {
                    when (it) {
                        is NavigationGroup -> it.entries
                        is NavigationItem -> listOf(it)
                        is NavigationSeparator -> emptyList()
                    }
                }
                .flatten()
                .filterIsInstance<NavigationItem<T>>()
                .find { idProvider(item.route) == idProvider(it.route) }?.let { emit(it) }
            entries
        }

    public val clicks: Flow<NavigationItem<T>> = handleClicks
}
