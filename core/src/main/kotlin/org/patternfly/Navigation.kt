package org.patternfly

import dev.fritz2.binding.RootStore
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
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfNavigation(
    router: Router<T>,
    identifier: Identifier<T>,
    orientation: Orientation,
    tertiary: Boolean = false,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(Navigation(router, identifier, orientation, tertiary), content)

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

fun <T> NavigationItems<T>.pfNavigationItem(item: T, text: String): NavigationItem<T> =
    pfNavigationItem(item) { +text }

fun <T> NavigationItems<T>.pfNavigationItem(
    item: T,
    content: A.() -> Unit = {}
): NavigationItem<T> =
    register(NavigationItem(this.navigation, item, content), {})

// ------------------------------------------------------ tag

class Navigation<T>(
    internal val router: Router<T>,
    internal val identifier: Identifier<T>,
    private val orientation: Orientation,
    tertiary: Boolean
) : PatternFlyTag<HTMLElement>(ComponentType.Navigation, "nav", "nav".component()), Ouia {
    init {
        if (!tertiary) {
            attr("aria-label", "Global")
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
    Tag<HTMLLIElement>("li", baseClass = "${"nav".component("item")} ${"expandable".modifier()}") {

    private val expanded = ExpandableGroupStore()

    init {
        // don't use classMap for expanded flow
        // classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
        MainScope().launch {
            expanded.data.collect { domNode.classList.toggle("expanded".modifier(), it) }
        }
        // it might interfere with router flow, which also modified the class list
        MainScope().launch {
            this@NavigationExpandableGroup.navigation.router.routes.collect {
                delay(333) // wait a little bit before testing for the current modifier
                val selector = By.classname("nav".component("link"), "current".modifier())
                val containsCurrent = domNode.querySelector(selector) != null
                domNode.classList.toggle("current".modifier(), containsCurrent)
            }
        }
        val id = Id.unique("neg")
        a("nav".component("link"), id) {
            +text
            clicks handledBy this@NavigationExpandableGroup.expanded.toggle
            this@NavigationExpandableGroup.expanded.data.map { it.toString() }.bindAttr("aria-expanded")
            this@NavigationExpandableGroup.expanded.data.bindAttr("aria-expanded", domNode, false) {
                it.toString()
            }
            span("nav".component("toggle")) {
                span("nav".component("toggle", "icon")) {
                    pfIcon("angle-right".fas())
                }
            }
        }
        section("nav".component("subnav")) {
            attr("aria-labelledby", id)
            this@NavigationExpandableGroup.expanded.data.bindAttr("hidden", domNode)
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
    item: T,
    content: A.() -> Unit
) : Tag<HTMLLIElement>("li", baseClass = "nav".component("item")) {
    init {
        val itemId = navigation.identifier(item)
        a("nav".component("link")) {
            clicks.map { item } handledBy this@NavigationItem.navigation.router.navTo
            classMap = this@NavigationItem.navigation.router.routes.map {
                mapOf("current".modifier() to (this@NavigationItem.navigation.identifier(it) == itemId))
            }
            this@NavigationItem.navigation.router.routes
                .map { this@NavigationItem.navigation.identifier(it) == itemId }
                .bindAttr("aria-current", domNode) { "page" }
            content(this)
        }
    }
}

// ------------------------------------------------------ store

internal class ExpandableGroupStore : RootStore<Boolean>(false) {

    val toggle = handle { expanded ->
        !expanded
    }
}
