package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.TextElement
import dev.fritz2.routing.Router
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.patternfly.Modifier.current
import org.patternfly.Modifier.expandable
import org.patternfly.Modifier.horizontal
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfHorizontalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    tertiary: Boolean = false,
    classes: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> =
    register(Navigation(router, selected, Orientation.HORIZONTAL, tertiary, classes, content), {})

fun <T> HtmlElements.pfHorizontalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    tertiary: Boolean = false,
    modifier: Modifier,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> =
    register(Navigation(router, selected, Orientation.HORIZONTAL, tertiary, modifier.value, content), {})

fun <T> HtmlElements.pfVerticalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    classes: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> =
    register(Navigation(router, selected, Orientation.VERTICAL, false, classes, content), {})

fun <T> HtmlElements.pfVerticalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    modifier: Modifier,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> =
    register(Navigation(router, selected, Orientation.VERTICAL, false, modifier.value, content), {})

fun <T> Navigation<T>.pfNavigationGroup(
    text: String,
    classes: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationGroup<T> = register(NavigationGroup(this, text, classes, content), {})

fun <T> Navigation<T>.pfNavigationGroup(
    text: String,
    modifier: Modifier,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationGroup<T> = register(NavigationGroup(this, text, modifier.value, content), {})

fun <T> NavigationItems<T>.pfNavigationExpandableGroup(
    text: String,
    classes: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationExpandableGroup<T> = register(NavigationExpandableGroup(this.navigation, text, classes, content), {})

fun <T> NavigationItems<T>.pfNavigationExpandableGroup(
    text: String,
    modifier: Modifier,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationExpandableGroup<T> =
    register(NavigationExpandableGroup(this.navigation, text, modifier.value, content), {})

fun <T> Navigation<T>.pfNavigationItems(
    classes: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this, classes), content)

fun <T> Navigation<T>.pfNavigationItems(
    modifier: Modifier,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this, modifier.value), content)

fun <T> NavigationGroup<T>.pfNavigationItems(
    classes: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this.navigation, classes), content)

fun <T> NavigationGroup<T>.pfNavigationItems(
    modifier: Modifier,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this.navigation, modifier.value), content)

internal fun <T> TextElement.pfNavigationItems(
    navigation: Navigation<T>,
    classes: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(navigation, classes), content)

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    text: String,
    classes: String? = null,
    selected: ((route: T) -> Boolean)? = null
): NavigationItem<T> = pfNavigationItem(item, classes, selected) { +text }

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    text: String,
    modifier: Modifier,
    selected: ((route: T) -> Boolean)? = null
): NavigationItem<T> = pfNavigationItem(item, modifier, selected) { +text }

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    classes: String? = null,
    selected: ((route: T) -> Boolean)? = null,
    content: A.() -> Unit = {}
): NavigationItem<T> =
    register(NavigationItem(this.navigation, item, selected, classes, content), {})

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    modifier: Modifier,
    selected: ((route: T) -> Boolean)? = null,
    content: A.() -> Unit = {}
): NavigationItem<T> =
    register(NavigationItem(this.navigation, item, selected, modifier.value, content), {})

// ------------------------------------------------------ tag

class Navigation<T>(
    internal val router: Router<T>,
    internal val selected: (route: T, item: T) -> Boolean,
    orientation: Orientation,
    tertiary: Boolean,
    classes: String?,
    content: Navigation<T>.() -> Unit
) : PatternFlyComponent<HTMLElement>,
    TextElement("nav", baseClass = classes {
        +ComponentType.Navigation
        +(horizontal `when` (orientation == Orientation.HORIZONTAL))
        +classes
    }) {

    init {
        markAs(ComponentType.Navigation)
        if (!tertiary) {
            attr("aria-label", "Global")
        }
        if (orientation == Orientation.HORIZONTAL) {
            // domNode.classList += "scrollable".modifier() // TODO Implement scrolling
            button("nav".component("scroll", "button")) {
                attr("aria-label", "Scroll left")
                disabled = const(true) // TODO Implement scrolling
                pfIcon("angle-left".fas())
            }
        }
        content(this)
        if (orientation == Orientation.HORIZONTAL) {
            button("nav".component("scroll", "button")) {
                attr("aria-label", "Scroll right")
                disabled = const(true) // TODO Implement scrolling
                pfIcon("angle-right".fas())
            }
        }
    }
}

class NavigationGroup<T>(
    internal val navigation: Navigation<T>,
    text: String,
    classes: String?,
    content: NavigationItems<T>.() -> Unit
) : Tag<HTMLElement>("section", baseClass = classes("nav".component("section"), classes)) {
    init {
        val id = Id.unique("ng")
        attr("aria-label", id)
        h2("nav".component("section", "title"), id) { +text }
        pfNavigationItems {
            content(this)
        }
    }
}

class NavigationExpandableGroup<T>(
    private val navigation: Navigation<T>,
    text: String,
    classes: String?,
    content: NavigationItems<T>.() -> Unit
) : Tag<HTMLLIElement>("li", baseClass = classes {
    +"nav".component("item")
    +expandable
    +classes
}) {

    private val expanded = CollapseExpandStore()

    init {
        // don't use classMap for expanded flow
        // classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
        MainScope().launch {
            expanded.data.collect { domNode.classList.toggle(Modifier.expanded.value, it) }
        }
        // it might interfere with router flow, which also modifies the class list
        MainScope().launch {
            this@NavigationExpandableGroup.navigation.router.collect {
                delay(333) // wait a little bit before testing for the current modifier
                val selector = By.classname("nav".component("link"), current.value)
                val containsCurrent = domNode.querySelector(selector) != null
                domNode.classList.toggle(current.value, containsCurrent)
            }
        }
        val id = Id.unique("neg")
        a("nav".component("link"), id) {
            +text
            clicks handledBy this@NavigationExpandableGroup.expanded.toggle
            this@NavigationExpandableGroup.expanded.data.map { it.toString() }.bindAttr("aria-expanded")

            span("nav".component("toggle")) {
                span("nav".component("toggle", "icon")) {
                    pfIcon("angle-right".fas())
                }
            }
        }
        section("nav".component("subnav")) {
            attr("aria-labelledby", id)
            this@NavigationExpandableGroup.expanded.data.map { !it }.bindAttr("hidden")
            pfNavigationItems(this@NavigationExpandableGroup.navigation) {
                content(this)
            }
        }
    }
}

class NavigationItems<T>(internal val navigation: Navigation<T>, classes: String?) :
    Tag<HTMLUListElement>("ul", baseClass = classes("nav".component("list"), classes))

class NavigationItem<T>(
    private val navigation: Navigation<T>,
    private val item: T,
    private val selected: ((route: T) -> Boolean)?,
    classes: String?,
    content: A.() -> Unit
) : Tag<HTMLLIElement>("li", baseClass = classes("nav".component("item"), classes)) {
    init {
        a("nav".component("link")) {
            clicks.map { this@NavigationItem.item } handledBy this@NavigationItem.navigation.router.navTo
            classMap = this@NavigationItem.navigation.router.map { route ->
                mapOf(current.value to (this@NavigationItem.calculateSelection(route)))
            }
            this@NavigationItem.navigation.router
                .map { route -> this@NavigationItem.calculateSelection(route) }
                .bindAttr("aria-current", "page")
            content(this)
        }
    }

    private fun calculateSelection(route: T): Boolean {
        return selected?.invoke(route) ?: navigation.selected.invoke(route, item)
    }
}
