package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.Job
import org.patternfly.component.markAs
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
public fun RenderContext.pageGroup(
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
    content: Tag<HTMLElement>.() -> Unit = {}
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
    content: Tag<HTMLElement>.() -> Unit = {}
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
    content: Tag<HTMLElement>.() -> Unit = {}
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
    content: Tag<HTMLElement>.() -> Unit = {}
): PageSection = if (limitWidth) {
    register(
        PageSection(
            sticky,
            id = id,
            baseClass = classes {
                +pageSectionClass
                +("limit-width".modifier() `when` limitWidth)
                +baseClass
            },
            job
        ),
        {
            with(it) {
                div(baseClass = "page".component("main", "body")) {
                    content(this)
                }
            }
        }
    )
} else {
    register(PageSection(sticky, id = id, baseClass = classes(pageSectionClass, baseClass), job), content)
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
 * ┃ │                          │ │                                          │ ┃
 * ┃ │ ┌──────────────────────┐ │ │ ┌──────── pageGroup: PageGroup ────────┐ │ ┃
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
    PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.Page, baseClass), job, Scope()) {

    internal val sidebarStore: SidebarStore = SidebarStore()

    init {
        markAs(ComponentType.Page)
    }
}

/**
 * Main component inside the [Page] component.
 */
public class PageMain internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLElement>,
    TextElement("main", id = id, baseClass = classes(ComponentType.PageMain, baseClass), job, Scope()) {

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
        job,
        scope = Scope()
    )

/**
 * Page section component.
 */
public class PageSection internal constructor(
    sticky: Sticky?,
    id: String?,
    baseClass: String?,
    job: Job
) : TextElement("section", id = id, baseClass = classes(sticky?.modifier, baseClass), job, Scope())
