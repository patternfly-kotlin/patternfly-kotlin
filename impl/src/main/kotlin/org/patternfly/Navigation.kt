package org.patternfly

import dev.fritz2.binding.RootStore
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
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(Navigation(router, selected, Orientation.HORIZONTAL, tertiary, content), {})

fun <T> HtmlElements.pfVerticalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(Navigation(router, selected, Orientation.VERTICAL, false, content), {})

fun <T> Navigation<T>.pfNavigationGroup(
    text: String,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationGroup<T> = register(NavigationGroup(this, text, content), {})

fun <T> NavigationItems<T>.pfNavigationExpandableGroup(
    text: String,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationExpandableGroup<T> = register(NavigationExpandableGroup(this.navigation, text, content), {})

fun <T> Navigation<T>.pfNavigationItems(
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this), content)

fun <T> NavigationGroup<T>.pfNavigationItems(
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this.navigation), content)

internal fun <T> TextElement.pfNavigationItems(
    navigation: Navigation<T>,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(navigation), content)

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    text: String,
    selected: ((route: T) -> Boolean)? = null
): NavigationItem<T> = pfNavigationItem(item, selected) { +text }

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    selected: ((route: T) -> Boolean)? = null,
    content: A.() -> Unit = {}
): NavigationItem<T> =
    register(NavigationItem(this.navigation, item, selected, content), {})

// ------------------------------------------------------ tag

class Navigation<T>(
    internal val router: Router<T>,
    internal val selected: (route: T, item: T) -> Boolean,
    orientation: Orientation,
    tertiary: Boolean,
    content: Navigation<T>.() -> Unit
) : TextElement("nav", baseClass = "nav".component()) {
    init {
        domNode.componentType(ComponentType.Navigation)
        if (!tertiary) {
            attr("aria-label", "Global")
        }
        if (orientation == Orientation.HORIZONTAL) {
            domNode.classList += horizontal
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
    content: NavigationItems<T>.() -> Unit
) : Tag<HTMLElement>("section", baseClass = "nav".component("section")) {
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
    content: NavigationItems<T>.() -> Unit
) :
    Tag<HTMLLIElement>("li", baseClass = "${"nav".component("item")} ${expandable.value}") {

    private val expanded = ExpandableGroupStore()

    init {
        // don't use classMap for expanded flow
        // classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
        MainScope().launch {
            expanded.data.collect { domNode.classList.toggle(Modifier.expanded.value, it) }
        }
        // it might interfere with router flow, which also modified the class list
        MainScope().launch {
            this@NavigationExpandableGroup.navigation.router.routes.collect {
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

class NavigationItems<T>(internal val navigation: Navigation<T>) :
    Tag<HTMLUListElement>("ul", baseClass = "nav".component("list"))

class NavigationItem<T>(
    private val navigation: Navigation<T>,
    private val item: T,
    private val selected: ((route: T) -> Boolean)?,
    content: A.() -> Unit
) : Tag<HTMLLIElement>("li", baseClass = "nav".component("item")) {
    init {
        a("nav".component("link")) {
            clicks.map { this@NavigationItem.item } handledBy this@NavigationItem.navigation.router.navTo
            classMap = this@NavigationItem.navigation.router.routes.map { route ->
                mapOf(current.value to (this@NavigationItem.calculateSelection(route)))
            }
            this@NavigationItem.navigation.router.routes
                .map { route -> this@NavigationItem.calculateSelection(route) }
                .bindAttr("aria-current", "page")
            content(this)
        }
    }

    private fun calculateSelection(route: T): Boolean {
        return selected?.invoke(route) ?: navigation.selected.invoke(route, item)
    }
}

// ------------------------------------------------------ store

internal class ExpandableGroupStore : RootStore<Boolean>(false) {

    val toggle = handle { expanded ->
        !expanded
    }
}
