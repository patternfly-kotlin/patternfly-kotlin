package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import kotlinx.browser.document
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.aria
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
 * Appends the page component to the body element.
 */
public fun Page.appendToBody() {
    document.body?.appendChild(this.domNode)
}

/**
 * Creates the [Header] component inside the [Page] component.
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
 * Creates the [Brand] component inside the [Header] component.
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
 * Creates a container for the tools inside the [Header] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Header.headerTools(
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
public fun Page.pageSidebar(
    id: String? = null,
    baseClass: String? = null,
    content: Sidebar.() -> Unit = {}
): Sidebar = register(Sidebar(sidebarStore, id = id, baseClass = baseClass, job), content)

/**
 * Creates the body container inside the [Sidebar] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Sidebar.sidebarBody(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): Div = register(Div(id = id, baseClass = classes("page".component("sidebar", "body"), baseClass), job), content)

/**
 * Creates the [PageMain] container inside the [Page].
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Page.pageMain(
    id: String? = null,
    baseClass: String? = null,
    content: PageMain.() -> Unit = {}
): PageMain = register(PageMain(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageSection] container.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.pageSection(
    id: String? = null,
    baseClass: String? = null,
    content: PageSection.() -> Unit = {}
): PageSection = register(PageSection(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [page](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * A page component is used to create the basic structure of an application. It should be added directly to the
 * document body.
 *
 * Typically a page contains some but not necessarily all of the following components.
 *
 * ```
 * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ page: Page ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
 * ┃                                                                           ┃
 * ┃ ┌──────────────────────── pageHeader: PageHeader ───────────────────────┐ ┃
 * ┃ │ ┌──────────────┐ ┌─────────────────────────────┐ ┌──────────────────┐ │ ┃
 * ┃ │ │              │ │    horizontalNavigation:    │ │                  │ │ ┃
 * ┃ │ │ brand: Brand │ │         Navigation          │ │ headerTools: Div │ │ ┃
 * ┃ │ │              │ │                             │ │                  │ │ ┃
 * ┃ │ └──────────────┘ └─────────────────────────────┘ └──────────────────┘ │ ┃
 * ┃ └───────────────────────────────────────────────────────────────────────┘ ┃
 * ┃                                                                           ┃
 * ┃ ┌─── pageSidebar: PageSidebar ───┐ ┌──────── pageMain: PageMain ────────┐ ┃
 * ┃ │                                │ │                                    │ ┃
 * ┃ │ ┌───── sidebarBody: Div ─────┐ │ │ ┌────────────────────────────────┐ │ ┃
 * ┃ │ │ ┌────────────────────────┐ │ │ │ │                                │ │ ┃
 * ┃ │ │ │                        │ │ │ │ │                                │ │ ┃
 * ┃ │ │ │                        │ │ │ │ │                                │ │ ┃
 * ┃ │ │ │                        │ │ │ │ │                                │ │ ┃
 * ┃ │ │ │  verticalNavigation:   │ │ │ │ │    pageSection: PageSection    │ │ ┃
 * ┃ │ │ │       Navigation       │ │ │ │ │                                │ │ ┃
 * ┃ │ │ │                        │ │ │ │ │                                │ │ ┃
 * ┃ │ │ │                        │ │ │ │ │                                │ │ ┃
 * ┃ │ │ │                        │ │ │ │ │                                │ │ ┃
 * ┃ │ │ └────────────────────────┘ │ │ │ │                                │ │ ┃
 * ┃ │ └────────────────────────────┘ │ │ └────────────────────────────────┘ │ ┃
 * ┃ └────────────────────────────────┘ └────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 *
 * A page setup with a header, brand, tools, sidebar and navigation might look like this:
 *
 * @sample org.patternfly.sample.PageSample.typicalSetup
 */
public class Page internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Page, baseClass), job) {

    internal val sidebarStore: SidebarStore = SidebarStore()

    init {
        markAs(ComponentType.Page)
    }
}

/**
 * Header component.
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
 *
 * A brand is used to place a product logotype on a screen.
 */
public class Brand internal constructor(sidebarStore: SidebarStore, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "brand"), baseClass), job) {

    private var link: A
    private lateinit var img: Img

    init {
        div(baseClass = "page".component("header", "brand", "toggle")) {
            attr("hidden", sidebarStore.data.map { !it.visible })
            classMap(sidebarStore.data.map { mapOf("display-none".util() to it.visible) })
            clickButton(plain) {
                aria["expanded"] = sidebarStore.data.map { it.expanded.toString() }
                icon("bars".fas())
            } handledBy sidebarStore.toggle
        }
        this@Brand.link = a(baseClass = "page".component("header", "brand", "link")) {
            href("#")
            this@Brand.img = img(baseClass = "brand".component()) {}
        }
    }

    /**
     * Sets the link to the homepage of the application.
     */
    public fun home(href: String) {
        link.href(href)
    }

    /**
     * Sets the image for the brand.
     */
    public fun img(src: String, content: Img.() -> Unit = {}) {
        img.apply(content).src(src)
    }
}

/**
 * Sidebar component.
 *
 * If a sidebar is added to the page, a toggle button is displayed in the [Header] to toggle and expand the sidebar.
 *
 * To show & hide the sidebar (e.g. because some views don't require a sidebar) you can use the [visible] functions.
 */
public class Sidebar internal constructor(
    private val sidebarStore: SidebarStore,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.PageSidebar, baseClass), job) {

    init {
        markAs(ComponentType.PageSidebar)
        attr("hidden", sidebarStore.data.map { !it.visible })
        classMap(
            sidebarStore.data.map {
                mapOf(
                    "display-none".util() to !it.visible,
                    "collapsed".modifier() to !it.expanded,
                    "expanded".modifier() to it.expanded
                )
            }
        )
    }

    /**
     * Manually show & hide the sidebar.
     */
    public fun visible(value: Boolean) {
        sidebarStore.visible(value)
    }

    /**
     * Collects the values from the stream to show & hide the sidebar.
     */
    public fun visible(value: Flow<Boolean>) {
        mountSingle(job, value) { v, _ ->
            visible(v)
        }
    }
}

/**
 * Main component inside the [Page] component.
 */
public class PageMain internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLElement>,
    TextElement("main", id = id, baseClass = classes(ComponentType.Main, baseClass), job) {

    init {
        markAs(ComponentType.Main)
        attr("role", "main")
        attr("tabindex", "-1")
    }
}

/**
 * Page section component inside the [PageMain] component.
 */
public class PageSection internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLElement>,
    TextElement("section", id = id, baseClass = classes(ComponentType.Section, baseClass), job) {

    init {
        markAs(ComponentType.Section)
    }
}

// ------------------------------------------------------ store

internal data class SidebarStatus(val visible: Boolean, val expanded: Boolean)

internal class SidebarStore : RootStore<SidebarStatus>(SidebarStatus(visible = false, expanded = true)) {

    val visible: SimpleHandler<Boolean> = handle { status, visible ->
        status.copy(visible = visible)
    }

    val toggle: SimpleHandler<Unit> = handle { it.copy(expanded = !it.expanded) }
}
