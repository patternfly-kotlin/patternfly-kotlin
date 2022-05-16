@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.Ul
import dev.fritz2.routing.Router
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.dom.By
import org.patternfly.dom.Id
import org.patternfly.dom.debug
import org.patternfly.dom.querySelector
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ factory

/**
 * Creates an [Navigation] component.
 *
 * @param router the router instance
 * @param expandable whether groups are expandable (only applies to vertical variant)
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun <T> RenderContext.navigation(
    router: Router<T>,
    expandable: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Navigation<T>.() -> Unit = {}
) {
    Navigation(router, expandable).apply(context).render(this, baseClass, id)
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
 * ### Routing
 *
 * The passed [Router] is used for navigation (updates the location & history) unless custom events are used for a [NavigationItem].
 *
 * @sample org.patternfly.sample.NavigationSample.horizontal
 * @sample org.patternfly.sample.NavigationSample.horizontalSubNav
 * @sample org.patternfly.sample.NavigationSample.vertical
 */
@Suppress("TooManyFunctions")
public open class Navigation<T>(
    private val router: Router<T>,
    private var expandable: Boolean
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private lateinit var root: TextElement
    private lateinit var ul: Ul
    private val scrollStore = ScrollButtonStore()
    private val entries: MutableList<NavigationEntry<T>> = mutableListOf()

    /**
     * Modifies the [expandable] flag.
     */
    public fun expandable(expandable: Boolean) {
        this.expandable = expandable
    }

    /**
     * Adds a group and items.
     */
    public fun group(title: String, context: NavigationGroup<T>.() -> Unit) {
        entries.add(NavigationGroup<T>(title).apply(context))
    }

    /**
     * Adds an item.
     */
    public fun item(route: T, title: String, context: NavigationItem<T>.() -> Unit = {}) {
        entries.add(NavigationItem(route, title).apply(context))
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
                applyElement(this)
                applyEvents(this)

                if (variant == NavigationVariant.HORIZONTAL || variant == NavigationVariant.SUBNAV) {
                    classMap(scrollStore.data.map { mapOf("scrollable".modifier() to (it.showButtons)) })

                    // update scroll buttons, when window has been resized
                    ScrollButton.windowResizes().map { ul.domNode.updateScrollButtons() }
                        .filterNotNull() handledBy scrollStore.update
                    // initial update
                    (MainScope() + job).launch {
                        ul.domNode.updateScrollButtons()?.let { scrollStore.update(it) }
                    }
                }

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

    private fun horizontal(context: RenderContext, entries: List<NavigationEntry<T>>) {
        with(context) {
            leftScrollButton(ul.domNode, scrollStore)
            flat(this, entries, true)
            rightScrollButton(ul.domNode, scrollStore)
        }
    }

    private fun verticalGrouped(context: Tag<HTMLElement>, entries: List<NavigationEntry<T>>) {
        with(context) {
            entries.forEach { entry ->
                when (entry) {
                    is NavigationGroup<T> -> {
                        val headerId = Id.unique(ComponentType.Navigation.id, "grp")
                        section(baseClass = "nav".component("section")) {
                            aria["lebelledby"] = headerId
                            h2("nav".component("section", "title"), headerId) {
                                entry.applyTitle(this)
                            }
                            entry.applyEvents(this)
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
                    ScrollButton.scrolls(domNode).map {
                        domNode.updateScrollButtons()
                    }.filterNotNull() handledBy (scrollStore.update)
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
                group.entries.forEach { entry ->
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
                with(group.expandedStore) { toggleExpanded() }
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
                    group.applyTitle(this)
                    clicks handledBy group.expandedStore.toggle
                    with(group.expandedStore) { toggleAriaExpanded() }
                    span("nav".component("toggle")) {
                        span("nav".component("toggle", "icon")) {
                            icon("angle-right".fas())
                        }
                    }
                }
                section("nav".component("subnav")) {
                    aria["labelledby"] = buttonId
                    with(group.expandedStore) { hideIfCollapsed() }
                    group.applyEvents(this)
                    group(this, group)
                }
            }
        }
    }

    private fun link(context: RenderContext, item: NavigationItem<T>) {
        with(context) {
            li(baseClass = "nav".component("item")) {
                a("nav".component("link")) {
                    if (item.hasEvents) {
                        // It's assumed that the user takes care of the navigation herself.
                        item.applyEvents(this)
                    } else {
                        clicks.map { item.route } handledBy router.navTo
                    }
                    classMap(
                        router.data.map { route ->
                            mapOf("current".modifier() to (route == item.route))
                        }
                    )
                    aria["current"] = router.data.map { route ->
                        if (route == item.route) "page" else ""
                    }
                    item.applyTitle(this)
                }
            }
        }
    }

    private fun warning(context: Tag<HTMLElement>, message: String) {
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
public class NavigationGroup<T> internal constructor(
    title: String,
    initialEntries: List<NavigationEntry<T>> = emptyList()
) : NavigationEntry<T>(),
    WithExpandedStore by ExpandedStoreMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal val entries: MutableList<NavigationEntry<T>> = mutableListOf()

    init {
        this.title(title)
        this.entries.addAll(initialEntries)
    }

    /**
     * Adds an item to this group.
     */
    public fun item(route: T, title: String, context: NavigationItem<T>.() -> Unit = {}) {
        entries.add(
            NavigationItem(route, title)
                .apply(context)
                .also { item ->
                    item.group = this
                }
        )
    }

    /**
     * Adds a separator to this group.
     */
    public fun separator() {
        entries.add(NavigationSeparator())
    }
}

/**
 * A navigation item with a route and a title.
 */
public class NavigationItem<T> internal constructor(public val route: T, title: String) :
    NavigationEntry<T>(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    internal val id: String = Id.unique(ComponentType.Navigation.id, "itm")
    internal var group: NavigationGroup<T>? = null

    init {
        this.title(title)
    }
}

/**
 * A navigation separator.
 */
public class NavigationSeparator<T> internal constructor() : NavigationEntry<T>()
