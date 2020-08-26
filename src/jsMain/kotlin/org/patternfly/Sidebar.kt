package org.patternfly

import dev.fritz2.dom.html.Div

// ------------------------------------------------------ dsl

fun Page.pfSidebar(content: Sidebar.() -> Unit = {}): Sidebar = register(Sidebar(), content)

fun Sidebar.pfSidebarBody(content: SidebarBody.() -> Unit = {}): SidebarBody = register(SidebarBody(), content)

// ------------------------------------------------------ tag

class Sidebar : Div(baseClass = "page".component("sidebar")) {
    init {
        domNode.componentType(ComponentType.Sidebar)
    }
}

class SidebarBody : Div(baseClass = "page".component("sidebar", "body"))
