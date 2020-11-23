package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import dev.fritz2.dom.html.render
import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.patternfly.ButtonVariation.plain
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates the [Page] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.page(
    id: String? = null,
    baseClass: String? = null,
    content: Page.() -> Unit = {}
): Page = register(Page(id = id, baseClass = baseClass, job), content)

/**
 * Creates the [Header] component inside the [Page].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Page.pageHeader(
    id: String? = null,
    baseClass: String? = null,
    content: Header.() -> Unit = {}
): Header = register(Header(this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [Brand] component inside the [Header].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Header.brand(
    id: String? = null,
    baseClass: String? = null,
    content: Brand.() -> Unit = {}
): Brand = register(Brand(this.page.sidebarStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates a container for the tools inside the [Header].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Header.tools(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("page".component("header", "tools"), baseClass), job), content)

/**
 * Creates the [Sidebar] component inside the [Page].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Page.sidebar(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Sidebar {
    val sidebar = register(Sidebar(this.sidebarStore, id = id, baseClass = baseClass, job, content), {})
    (MainScope() + job).launch {
        sidebarStore.show(Unit)
    }
    return sidebar
}

// ------------------------------------------------------ tag

/**
 * PatternFly [page](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * A page component is used to create the basic structure of an application. It should be added directly to the
 * document body.
 *
 * A typical page setup with a header, brand, tools, sidebar and navigation might look like this:
 *
 * @sample PageSamples.typicalSetup
 */
public class Page internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Page, baseClass), job) {

    internal val sidebarStore: SidebarStore = SidebarStore()

    init {
        markAs(ComponentType.Page)
    }
}

/**
 * [PatternFly header](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 */
public class Header internal constructor(internal val page: Page, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLElement>,
    TextElement("header", id = id, baseClass = classes(ComponentType.PageHeader, baseClass), job) {

    init {
        markAs(ComponentType.PageHeader)
        attr("role", "banner")
    }
}

/**
 * [PatternFly brand](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 */
public class Brand internal constructor(sidebarStore: SidebarStore, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "brand"), baseClass), job) {

    private var link: A
    private lateinit var image: Img

    init {
        div(baseClass = "page".component("header", "brand", "toggle")) {
            attr("hidden", sidebarStore.data.map { !it.visible })
            classMap(sidebarStore.data.map { mapOf("display-none".util() to it.visible) })
            button(plain) {
                attr("aria-expanded", sidebarStore.data.map { it.expanded.toString() })
                icon("bars".fas())
                clicks handledBy sidebarStore.toggle
            }
        }
        this@Brand.link = a(baseClass = "page".component("header", "brand", "link")) {
            href("#")
            this@Brand.image = img(baseClass = "brand".component()) {}
        }
    }

    public fun home(href: String) {
        link.href(href)
    }

    public fun image(src: String, content: Img.() -> Unit = {}) {
        image.apply(content).src(src)
    }
}

/**
 * [PatternFly sidebar](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * If a sidebar is added to the page, a toggle button is displayed in the [Header] to toggle and expand the sidebar.
 * If you want to show hide the sidebar manually (e.g. because some views don't require a sidebar), please use
 * [SidebarStore].
 */
public class Sidebar internal constructor(
    sidebarStore: SidebarStore,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Div.() -> Unit
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.PageSidebar, baseClass), job) {

    init {
        markAs(ComponentType.PageSidebar)
        attr("hidden", sidebarStore.data.map { !it.visible })
        classMap(sidebarStore.data.map {
            mapOf(
                "display-none".util() to it.visible,
                "collapsed".modifier() to !it.expanded,
                "expanded".modifier() to it.expanded
            )
        })
        div(baseClass = "page".component("sidebar", "body")) {
            content(this)
        }
    }
}

// ------------------------------------------------------ store

public data class SidebarStatus(val visible: Boolean, val expanded: Boolean)

public class SidebarStore : RootStore<SidebarStatus>(SidebarStatus(visible = false, expanded = true)) {

    public val show: SimpleHandler<Unit> = handle { it.copy(visible = false) }
    public val toggle: SimpleHandler<Unit> = handle { it.copy(expanded = !it.expanded) }
}
