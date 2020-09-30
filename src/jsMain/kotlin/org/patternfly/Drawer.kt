package org.patternfly

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDrawer(
    id: String? = null,
    baseClass: String? = null,
    content: Drawer.() -> Unit = {}
): Drawer = register(Drawer(id = id, baseClass = baseClass), content)

fun Drawer.pfDrawerSection(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerSection.() -> Unit = {}
): DrawerSection = register(DrawerSection(id = id, baseClass = baseClass), content)

fun Drawer.pfDrawerMain(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerMain.() -> Unit = {}
): DrawerMain = register(DrawerMain(this, id = id, baseClass = baseClass), content)

fun DrawerMain.pfDrawerContent(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerContent.() -> Unit = {}
): DrawerContent =
    register(DrawerContent(this.drawer, id = id, baseClass = baseClass), content)

fun DrawerMain.pfDrawerPanel(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerPanel.() -> Unit = {}
): DrawerPanel =
    register(DrawerPanel(this.drawer, id = id, baseClass = baseClass), content)

fun DrawerContent.pfDrawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass), content)

fun DrawerPanel.pfDrawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass), content)

fun DrawerBody.pfDrawerHead(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerHead = register(DrawerHead(this.drawer, id = id, baseClass = baseClass), content)

fun DrawerHead.pfDrawerActions(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerActions.() -> Unit = {}
): DrawerActions = register(DrawerActions(this.drawer, id = id, baseClass = baseClass), content)

fun DrawerActions.pfDrawerClose(
    id: String? = null,
    baseClass: String? = null
): DrawerClose {
    return register(DrawerClose(this.drawer, id = id, baseClass = baseClass), {})
}

// ------------------------------------------------------ tag

class Drawer internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Drawer, baseClass)) {

    val expanded = CollapseExpandStore()

    init {
        markAs(ComponentType.Drawer)
        classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

class DrawerActions(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("actions"), baseClass))

class DrawerBody(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("body"), baseClass))

class DrawerClose(private val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("close"), baseClass)) {
    init {
        pfButton("plain".modifier()) {
            pfIcon("times".fas())
            attr("aria-label", "Close drawer panel")
            clicks.map { false } handledBy this@DrawerClose.drawer.expanded.update
        }
    }
}

class DrawerContent(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("content"), baseClass))

class DrawerHead(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("head"), baseClass))

class DrawerMain(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("main"), baseClass))

class DrawerPanel(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("panel"), baseClass)) {
    init {
        if (!drawer.domNode.classList.contains("static".modifier())) {
            drawer.expanded.data.map { !it }.bindAttr("hidden")
        }
    }
}

class DrawerSection(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("section"), baseClass))
