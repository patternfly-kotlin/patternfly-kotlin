package org.patternfly

import dev.fritz2.dom.html.Div
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun Page.pfSidebar(
    id: String? = null,
    classes: String? = null,
    content: Sidebar.() -> Unit = {}
): Sidebar = register(Sidebar(id = id, classes = classes), content)

fun Sidebar.pfSidebarBody(
    id: String? = null,
    classes: String? = null,
    content: SidebarBody.() -> Unit = {}
): SidebarBody = register(SidebarBody(id = id, classes = classes), content)

// ------------------------------------------------------ tag

class Sidebar(id: String?, classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Sidebar, classes)) {
    init {
        markAs(ComponentType.Sidebar)
    }
}

class SidebarBody(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("page".component("sidebar", "body"), classes))
