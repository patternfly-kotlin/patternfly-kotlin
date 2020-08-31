package org.patternfly

import dev.fritz2.dom.html.Div
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun Page.pfSidebar(classes: String? = null, content: Sidebar.() -> Unit = {}): Sidebar =
    register(Sidebar(classes), content)

fun Page.pfSidebar(modifier: Modifier, content: Sidebar.() -> Unit = {}): Sidebar =
    register(Sidebar(modifier.value), content)

fun Sidebar.pfSidebarBody(classes: String? = null, content: SidebarBody.() -> Unit = {}): SidebarBody =
    register(SidebarBody(classes), content)

fun Sidebar.pfSidebarBody(modifier: Modifier, content: SidebarBody.() -> Unit = {}): SidebarBody =
    register(SidebarBody(modifier.value), content)

// ------------------------------------------------------ tag

class Sidebar(classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.Sidebar, classes)) {
    init {
        markAs(ComponentType.Sidebar)
    }
}

class SidebarBody(classes: String?) : Div(baseClass = classes("page".component("sidebar", "body"), classes))
