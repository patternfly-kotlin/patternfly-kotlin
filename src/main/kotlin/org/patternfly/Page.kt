package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
 * Creates the [PageHeader] component inside the [Page] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Page.pageHeader(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeader.() -> Unit = {}
): PageHeader = register(PageHeader(this, id = id, baseClass = baseClass, job), content)

/**
 * Creates the [PageHeaderTools] component inside the [PageHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeader.pageHeaderTools(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderTools.() -> Unit = {}
): PageHeaderTools = register(PageHeaderTools(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageHeaderToolsGroup] component inside the [PageHeaderTools] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeaderTools.pageHeaderToolsGroup(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderToolsGroup.() -> Unit = {}
): PageHeaderToolsGroup = register(PageHeaderToolsGroup(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageHeaderToolsItem] component inside a [PageHeaderToolsGroup] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeaderToolsGroup.pageHeaderToolsItem(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderToolsItem.() -> Unit = {}
): PageHeaderToolsItem = register(PageHeaderToolsItem(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageHeaderToolsItem] component inside the [PageHeaderTools] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun PageHeaderTools.pageHeaderToolsItem(
    id: String? = null,
    baseClass: String? = null,
    content: PageHeaderToolsItem.() -> Unit = {}
): PageHeaderToolsItem = register(PageHeaderToolsItem(id = id, baseClass = baseClass, job), content)

/**
 * Creates the [PageSidebar] component inside the [Page] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the sidebar body
 */
public fun Page.pageSidebar(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): PageSidebar = register(PageSidebar(sidebarStore, id = id, baseClass = baseClass, job), {
    div(baseClass = "page".component("sidebar", "body")) {
        content(this)
    }
})

/**
 * Creates the [PageMain] component inside the [Page] component.
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
 * Creates a [PageGroup] component inside the [PageMain] component.
 *
 * @param sticky whether the component should be sticky
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the sidebar body
 */
public fun PageMain.pageGroup(
    sticky: Sticky? = null,
    id: String? = null,
    baseClass: String? = null,
    content: PageGroup.() -> Unit = {}
): PageGroup = register(PageGroup(sticky, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PageSection] component for adding a page navigation component.
 *
 * @param sticky whether the component should be sticky
 * @param limitWidth whether the page section limits the `max-width` of the content inside.
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the content
 */
public fun RenderContext.pageNavigation(
    sticky: Sticky? = null,
    limitWidth: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: RenderContext.() -> Unit = {}
): PageSection = genericPageSection(
    sticky,
    limitWidth,
    "page".component("main", "nav"),
    id,
    baseClass,
    content
)

/**
 * Creates a [PageSection] component for adding a page breadcrumb component.
 *
 * @param sticky whether the component should be sticky
 * @param limitWidth whether the page section limits the `max-width` of the content inside.
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the content
 */
public fun RenderContext.pageBreadcrumb(
    sticky: Sticky? = null,
    limitWidth: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: RenderContext.() -> Unit = {}
): PageSection = genericPageSection(
    sticky,
    limitWidth,
    "page".component("main", "breadcrumb"),
    id,
    baseClass,
    content
)

/**
 * Creates a [PageSection] component.
 *
 * @param sticky whether the component should be sticky
 * @param limitWidth whether the page section limits the `max-width` of the content inside.
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the content
 */
public fun RenderContext.pageSection(
    sticky: Sticky? = null,
    limitWidth: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: RenderContext.() -> Unit = {}
): PageSection = genericPageSection(
    sticky,
    limitWidth,
    "page".component("main", "section"),
    id,
    baseClass,
    content
)

private fun RenderContext.genericPageSection(
    sticky: Sticky?,
    limitWidth: Boolean,
    pageSectionClass: String,
    id: String? = null,
    baseClass: String? = null,
    content: RenderContext.() -> Unit = {}
): PageSection = if (limitWidth) {
    register(PageSection(sticky, id = id, baseClass = classes(pageSectionClass, baseClass), job), {
        div(baseClass = "page".component("main", "body")) {
            content(this)
        }
    })
} else {
    register(PageSection(sticky, id = id, baseClass = baseClass, job), content)
}

// ------------------------------------------------------ tag

/**
 * PatternFly [page](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * The page component is used to define the basic layout of a page with either vertical or horizontal navigation. It should be added directly to the document body.
 *
 * Typically a page contains some but not necessarily all of the following components.
 *
 * ```
 * ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ page: Page ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
 * ┃                                                                           ┃
 * ┃ ┌──────────────────────── pageHeader: PageHeader ───────────────────────┐ ┃
 * ┃ │                                                                       │ ┃
 * ┃ │ ┌──────────┐ ┌─────────────────────┐ ┌────── pageHeaderTools: ──────┐ │ ┃
 * ┃ │ │          │ │                     │ │       PageHeaderTools        │ │ ┃
 * ┃ │ │          │ │                     │ │                              │ │ ┃
 * ┃ │ │          │ │                     │ │ ┌── pageHeaderToolsGroup: ─┐ │ │ ┃
 * ┃ │ │          │ │                     │ │ │   PageHeaderToolsGroup   │ │ │ ┃
 * ┃ │ │          │ │                     │ │ │ ┌──────────────────────┐ │ │ │ ┃
 * ┃ │ │  brand:  │ │horizontalNavigation:│ │ │ │ pageHeaderToolsItem: │ │ │ │ ┃
 * ┃ │ │  Brand   │ │     Navigation      │ │ │ │ PageHeaderToolsItem  │ │ │ │ ┃
 * ┃ │ │          │ │                     │ │ │ └──────────────────────┘ │ │ │ ┃
 * ┃ │ │          │ │                     │ │ └──────────────────────────┘ │ │ ┃
 * ┃ │ │          │ │                     │ │ ┌──────────────────────────┐ │ │ ┃
 * ┃ │ │          │ │                     │ │ │   pageHeaderToolsItem:   │ │ │ ┃
 * ┃ │ │          │ │                     │ │ │   PageHeaderToolsItem    │ │ │ ┃
 * ┃ │ │          │ │                     │ │ └──────────────────────────┘ │ │ ┃
 * ┃ │ └──────────┘ └─────────────────────┘ └──────────────────────────────┘ │ ┃
 * ┃ └───────────────────────────────────────────────────────────────────────┘ ┃
 * ┃                                                                           ┃
 * ┃ ┌ pageSidebar: PageSidebar ┐ ┌─────────── pageMain: PageMain ───────────┐ ┃
 * ┃ │ ┌──────────────────────┐ │ │                                          │ ┃
 * ┃ │ │                      │ │ │ ┌──────── pageGroup: PageGroup ────────┐ │ ┃
 * ┃ │ │                      │ │ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │                      │ │ │ │ │   pageNavigation: PageSection    │ │ │ ┃
 * ┃ │ │                      │ │ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ │                      │ │ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │                      │ │ │ │ │   pageBreadcrumb: PageSection    │ │ │ ┃
 * ┃ │ │                      │ │ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ │                      │ │ │ │ ┌──────────────────────────────────┐ │ │ ┃
 * ┃ │ │ verticalNavigation:  │ │ │ │ │     pageSection: PageSection     │ │ │ ┃
 * ┃ │ │      Navigation      │ │ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ │                      │ │ │ └──────────────────────────────────────┘ │ ┃
 * ┃ │ │                      │ │ │ ┌──────────────────────────────────────┐ │ ┃
 * ┃ │ │                      │ │ │ │     pageNavigation: PageSection      │ │ ┃
 * ┃ │ │                      │ │ │ └──────────────────────────────────────┘ │ ┃
 * ┃ │ │                      │ │ │ ┌──────────────────────────────────────┐ │ ┃
 * ┃ │ │                      │ │ │ │     pageBreadcrumb: PageSection      │ │ ┃
 * ┃ │ │                      │ │ │ └──────────────────────────────────────┘ │ ┃
 * ┃ │ │                      │ │ │ ┌──────────────────────────────────────┐ │ ┃
 * ┃ │ │                      │ │ │ │       pageSection: PageSection       │ │ ┃
 * ┃ │ └──────────────────────┘ │ │ └──────────────────────────────────────┘ │ ┃
 * ┃ └──────────────────────────┘ └──────────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
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
 * Page header or [masthead](https://www.patternfly.org/v4/components/page/design-guidelines/#masthead) component.
 */
public class PageHeader internal constructor(internal val page: Page, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLElement>,
    TextElement("header", id = id, baseClass = classes(ComponentType.PageHeader, baseClass), job) {

    init {
        markAs(ComponentType.PageHeader)
        attr("role", "banner")
    }
}

/**
 * Page header tools component.
 */
public class PageHeaderTools internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "tools"), baseClass), job)

/**
 * Page header tools group component.
 */
public class PageHeaderToolsGroup internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "tools", "group"), baseClass), job)

/**
 * Page header tools item component.
 */
public class PageHeaderToolsItem internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("page".component("header", "tools", "item"), baseClass), job)

/**
 * Sidebar component.
 *
 * If a sidebar is added to the page, a toggle button is displayed in the [PageHeader] to toggle and expand the sidebar.
 *
 * To show & hide the sidebar (e.g. because some views don't require a sidebar) you can use the [visible] functions.
 */
public class PageSidebar internal constructor(
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
        console.log("${if (value) "Show" else "Hide"} sidebar")
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
    TextElement("main", id = id, baseClass = classes(ComponentType.PageMain, baseClass), job) {

    init {
        markAs(ComponentType.PageMain)
        attr("role", "main")
        attr("tabindex", "-1")
    }
}

/**
 * Page group component.
 */
public class PageGroup internal constructor(sticky: Sticky?, id: String?, baseClass: String?, job: Job) :
    Div(
        id = id,
        baseClass = classes {
            +"page".component("main-group")
            +sticky?.modifier
            +baseClass
        },
        job
    )

/**
 * Page section component.
 */
public class PageSection internal constructor(
    sticky: Sticky?,
    id: String?,
    baseClass: String?,
    job: Job
) : TextElement("section", id = id, baseClass = classes(sticky?.modifier, baseClass), job)

// ------------------------------------------------------ store

internal data class SidebarStatus(val visible: Boolean, val expanded: Boolean)

internal class SidebarStore : RootStore<SidebarStatus>(SidebarStatus(visible = true, expanded = true)) {

    val visible: SimpleHandler<Boolean> = handle { status, visible ->
        status.copy(visible = visible)
    }

    val toggle: SimpleHandler<Unit> = handle { it.copy(expanded = !it.expanded) }
}
