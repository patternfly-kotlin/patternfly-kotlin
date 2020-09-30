package org.patternfly

import dev.fritz2.dom.html.Div
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun Page.pfSidebar(
    id: String? = null,
    baseClass: String? = null,
    content: Sidebar.() -> Unit = {}
): Sidebar = register(Sidebar(id = id, baseClass = baseClass), content)

fun Sidebar.pfSidebarBody(
    id: String? = null,
    baseClass: String? = null,
    content: SidebarBody.() -> Unit = {}
): SidebarBody = register(SidebarBody(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

class Sidebar(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Sidebar, baseClass)) {
    init {
        markAs(ComponentType.Sidebar)
    }
}

class SidebarBody(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("page".component("sidebar", "body"), baseClass))
