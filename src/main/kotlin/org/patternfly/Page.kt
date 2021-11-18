package org.patternfly

import dev.fritz2.binding.Handler
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.ScopeContext
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.primary
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates the [Page] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.page(
    baseClass: String? = null,
    id: String? = null,
    context: Page.() -> Unit = {}
) {
    Page().apply(context).render(this, baseClass, id)
}

public fun RenderContext.pageGroup(
    baseClass: String? = null,
    id: String? = null,
    context: RenderContext.() -> Unit = {}
) {
    div(baseClass = classes("page".component("main-group"), baseClass), id = id) {
        context(this)
    }
}

public fun RenderContext.pageSection(
    limitWidth: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: RenderContext.() -> Unit = {}
) {
    genericPageSection(
        limitWidth = limitWidth,
        baseClass = classes("page".component("main-section"), baseClass),
        id = id,
        scope = {},
        context = context
    )
}

public fun RenderContext.pageNav(
    limitWidth: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: RenderContext.() -> Unit = {}
) {
    genericPageSection(
        limitWidth = limitWidth,
        baseClass = classes("page".component("main", "nav"), baseClass),
        id = id,
        scope = {},
        context = context
    )
}

public fun RenderContext.pageSubNav(
    limitWidth: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: RenderContext.() -> Unit = {}
) {
    genericPageSection(
        limitWidth = limitWidth,
        baseClass = classes("page".component("main", "subnav"), baseClass),
        id = id,
        scope = {
            set(Scopes.PAGE_SUBNAV, true)
        },
        context = context
    )
}

public fun RenderContext.pageBreadcrumb(
    limitWidth: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: RenderContext.() -> Unit = {}
) {
    genericPageSection(
        limitWidth = limitWidth,
        baseClass = classes("page".component("main", "breadcrumb"), baseClass),
        id = id,
        scope = {},
        context = context
    )
}

internal fun RenderContext.genericPageSection(
    limitWidth: Boolean,
    baseClass: String?,
    id: String?,
    scope: (ScopeContext.() -> Unit),
    context: RenderContext.() -> Unit
) {
    section(baseClass = baseClass, id = id, scope = scope) {
        if (limitWidth) {
            div(baseClass = "page".component("main-body")) {
                context(this)
            }
        } else {
            context(this)
        }
    }
}

// ------------------------------------------------------ component

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
public class Page :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val sidebarStore: SidebarStore = SidebarStore()
    private var masthead: SubComponent<Masthead>? = null
    private var sidebar: SubComponent<RenderContext>? = null
    private var main: SubComponent<RenderContext>? = null

    public fun masthead(
        baseClass: String? = null,
        id: String? = null,
        context: Masthead.() -> Unit = {}
    ) {
        masthead = SubComponent(baseClass, id, context)
    }

    public fun sidebar(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit = {}
    ) {
        sidebar = SubComponent(baseClass, id, context)
    }

    public fun main(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit = {}
    ) {
        main = SubComponent(baseClass, id, context)
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes(ComponentType.Page, baseClass),
                id = id
            ) {
                markAs(ComponentType.Page)
                aria(this)
                element(this)
                events(this)

                val mainId = main?.id ?: Id.unique("page", "main")
                if (main != null) {
                    linkButton(primary, baseClass = classes("skip-to-content".component())) {
                        href("#$mainId")
                    }
                }

                masthead?.let { component ->
                    Masthead().apply(component.context).render(this, component.baseClass, component.id)
                }

                sidebar?.let { component ->
                    div(
                        baseClass = classes(ComponentType.Sidebar, component.baseClass),
                        id = component.id,
                        scope = {
                            set(Scopes.SIDEBAR, true)
                            set(Scopes.SIDEBAR_STORE, sidebarStore)
                        }
                    ) {
                        markAs(ComponentType.Sidebar)
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
                        div(baseClass = "page".component("sidebar", "body")) {
                            component.context(this)
                        }
                    }
                }

                main?.let { component ->
                    main(
                        baseClass = classes("page".component("main"), component.baseClass),
                        id = component.id
                    ) {
                        attr("role", "main")
                        attr("tabindex", "-1")
                        component.context(this)
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------ store

internal data class SidebarStatus(val visible: Boolean, val expanded: Boolean)

internal class SidebarStore : RootStore<SidebarStatus>(SidebarStatus(visible = true, expanded = true)) {
    val toggle: Handler<Unit> = handle { it.copy(expanded = !it.expanded) }
}
