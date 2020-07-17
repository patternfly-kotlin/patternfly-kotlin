package org.patternfly

import dev.fritz2.dom.Tag
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun Page.pfSidebar(content: Sidebar.() -> Unit = {}) = register(Sidebar(), content)

fun Sidebar.pfSidebarBody(content: SidebarBody.() -> Unit = {}) = register(SidebarBody(), content)

// ------------------------------------------------------ tag

class Sidebar :
    PatternFlyTag<HTMLDivElement>(ComponentType.Sidebar, "div", "page".component("sidebar")), Ouia

class SidebarBody : Tag<HTMLDivElement>("div", baseClass = "page".component("sidebar", "body"))
