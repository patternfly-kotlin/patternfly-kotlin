package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.elemento.By
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.querySelector
import dev.fritz2.routing.Router
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLLIElement
import org.w3c.dom.HTMLUListElement

// ------------------------------------------------------ dsl

public fun <T> RenderContext.horizontalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    tertiary: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(
    Navigation(router, selected, Orientation.HORIZONTAL, tertiary, id = id, baseClass = baseClass, job, content),
    {})

public fun <T> RenderContext.verticalNavigation(
    router: Router<T>,
    selected: (route: T, item: T) -> Boolean = { route, item -> route == item },
    id: String? = null,
    baseClass: String? = null,
    content: Navigation<T>.() -> Unit = {}
): Navigation<T> = register(
    Navigation(router, selected, Orientation.VERTICAL, false, id = id, baseClass = baseClass, job, content),
    {})

public fun <T> Navigation<T>.group(
    text: String,
    id: String? = null,
    baseClass: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationGroup<T> = register(NavigationGroup(this, text, id = id, baseClass = baseClass, job, content), {})

public fun <T> NavigationItems<T>.expandableGroup(
    text: String,
    id: String? = null,
    baseClass: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationExpandableGroup<T> =
    register(NavigationExpandableGroup(this.navigation, text, id = id, baseClass = baseClass, job, content), {})

public fun <T> Navigation<T>.items(
    id: String? = null,
    baseClass: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this, id = id, baseClass = baseClass, job), content)

public fun <T> NavigationGroup<T>.items(
    id: String? = null,
    baseClass: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(this.navigation, id = id, baseClass = baseClass, job), content)

internal fun <T> TextElement.items(
    navigation: Navigation<T>,
    id: String? = null,
    baseClass: String? = null,
    content: NavigationItems<T>.() -> Unit = {}
): NavigationItems<T> = register(NavigationItems(navigation, id = id, baseClass = baseClass, job), content)

public fun <T> NavigationItems<T>.navigationItem(
    item: T,
    text: String,
    id: String? = null,
    baseClass: String? = null,
    selected: ((route: T) -> Boolean)? = null
): NavigationItem<T> = navigationItem(item, id, baseClass, selected) { +text }

public fun <T> NavigationItems<T>.navigationItem(
    item: T,
    id: String? = null,
    baseClass: String? = null,
    selected: ((route: T) -> Boolean)? = null,
    content: A.() -> Unit = {}
): NavigationItem<T> =
    register(NavigationItem(this.navigation, item, selected, id = id, baseClass = baseClass, job, content), {})

// ------------------------------------------------------ tag

public class Navigation<T> internal constructor(
    internal val router: Router<T>,
    internal val selected: (route: T, item: T) -> Boolean,
    orientation: Orientation,
    tertiary: Boolean,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Navigation<T>.() -> Unit
) : PatternFlyComponent<HTMLElement>,
    TextElement("nav", id = id, baseClass = classes {
        +ComponentType.Navigation
        +("horizontal".modifier() `when` (orientation == Orientation.HORIZONTAL))
        +baseClass
    }, job) {

    init {
        markAs(ComponentType.Navigation)
        if (!tertiary) {
            attr("aria-label", "Global")
        }
        if (orientation == Orientation.HORIZONTAL) {
            // domNode.classList += "scrollable".modifier() // TODO Implement scrolling
            button("nav".component("scroll", "button")) {
                attr("aria-label", "Scroll left")
                disabled(true) // TODO Implement scrolling
                icon("angle-left".fas())
            }
        }
        content(this)
        if (orientation == Orientation.HORIZONTAL) {
            button("nav".component("scroll", "button")) {
                attr("aria-label", "Scroll right")
                disabled(true) // TODO Implement scrolling
                icon("angle-right".fas())
            }
        }
    }
}

public class NavigationGroup<T> internal constructor(
    internal val navigation: Navigation<T>,
    text: String,
    id: String?,
    baseClass: String?,
    job: Job,
    content: NavigationItems<T>.() -> Unit
) : Tag<HTMLElement>("section", id = id, baseClass = classes("nav".component("section"), baseClass), job) {
    init {
        h2("nav".component("section", "title"), id) { +text }
        items {
            content(this)
        }
    }
}

public class NavigationExpandableGroup<T> internal constructor(
    private val navigation: Navigation<T>,
    text: String,
    id: String?,
    baseClass: String?,
    job: Job,
    content: NavigationItems<T>.() -> Unit
) : Tag<HTMLLIElement>("li", id = id, baseClass = classes {
    +"nav".component("item")
    +"expandable".modifier()
    +baseClass
}, job) {
    private val expanded = CollapseExpandStore()

    init {
        // don't use classMap for expanded flow
        // classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
        MainScope().launch {
            expanded.data.collect { domNode.classList.toggle("expanded".modifier(), it) }
        }
        // it might interfere with router flow, which also modifies the class list
        MainScope().launch {
            this@NavigationExpandableGroup.navigation.router.collect {
                delay(333) // wait a little bit before testing for the current modifier
                val selector = By.classname("nav".component("link"), "current".modifier())
                val containsCurrent = domNode.querySelector(selector) != null
                domNode.classList.toggle("current".modifier(), containsCurrent)
            }
        }
        val linkId = Id.unique(ComponentType.Navigation.id, "eg")
        a("nav".component("link"), linkId) {
            +text
            clicks handledBy this@NavigationExpandableGroup.expanded.toggle
            attr("aria-expanded", this@NavigationExpandableGroup.expanded.data.map { it.toString() })

            span("nav".component("toggle")) {
                span("nav".component("toggle", "icon")) {
                    icon("angle-right".fas())
                }
            }
        }
        section("nav".component("subnav")) {
            attr("aria-labelledby", linkId)
            attr("hidden", this@NavigationExpandableGroup.expanded.data.map { !it })
            items(navigation) {
                content(this)
            }
        }
    }
}

public class NavigationItems<T> internal constructor(
    internal val navigation: Navigation<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : Tag<HTMLUListElement>("ul", id = id, baseClass = classes("nav".component("list"), baseClass), job)

public class NavigationItem<T> internal constructor(
    private val navigation: Navigation<T>,
    private val item: T,
    private val selected: ((route: T) -> Boolean)?,
    id: String?,
    baseClass: String?,
    job: Job,
    content: A.() -> Unit
) : Tag<HTMLLIElement>("li", id = id, baseClass = classes("nav".component("item"), baseClass), job) {
    init {
        a("nav".component("link")) {
            clicks.map { this@NavigationItem.item } handledBy this@NavigationItem.navigation.router.navTo
            classMap(this@NavigationItem.navigation.router.map { route ->
                mapOf("current".modifier() to (this@NavigationItem.calculateSelection(route)))
            })
            attr("aria-current", this@NavigationItem.navigation.router
                .map { route -> if (this@NavigationItem.calculateSelection(route)) "page" else "" }
            )
            content(this)
        }
    }

    private fun calculateSelection(route: T): Boolean {
        return selected?.invoke(route) ?: navigation.selected.invoke(route, item)
    }
}
