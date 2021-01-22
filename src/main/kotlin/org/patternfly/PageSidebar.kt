package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.Div
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

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
): PageSidebar = register(
    PageSidebar(
        sidebarStore,
        id = id,
        baseClass = baseClass,
        job
    ),
    {
        div(baseClass = "page".component("sidebar", "body")) {
            content(this)
        }
    }
)

// ------------------------------------------------------ tag

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

// ------------------------------------------------------ store

internal data class SidebarStatus(val visible: Boolean, val expanded: Boolean)

internal class SidebarStore : RootStore<SidebarStatus>(SidebarStatus(visible = true, expanded = true)) {

    val visible: SimpleHandler<Boolean> = handle { status, visible ->
        status.copy(visible = visible)
    }

    val toggle: SimpleHandler<Unit> = handle { it.copy(expanded = !it.expanded) }
}
