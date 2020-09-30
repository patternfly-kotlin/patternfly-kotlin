package org.patternfly

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDrawer(
    id: String? = null,
    classes: String? = null,
    content: Drawer.() -> Unit = {}
): Drawer = register(Drawer(id = id, classes = classes), content)

fun Drawer.pfDrawerSection(
    id: String? = null,
    classes: String? = null,
    content: DrawerSection.() -> Unit = {}
): DrawerSection = register(DrawerSection(id = id, classes = classes), content)

fun Drawer.pfDrawerMain(
    id: String? = null,
    classes: String? = null,
    content: DrawerMain.() -> Unit = {}
): DrawerMain = register(DrawerMain(this, id = id, classes = classes), content)

fun DrawerMain.pfDrawerContent(
    id: String? = null,
    classes: String? = null,
    content: DrawerContent.() -> Unit = {}
): DrawerContent =
    register(DrawerContent(this.drawer, id = id, classes = classes), content)

fun DrawerMain.pfDrawerPanel(
    id: String? = null,
    classes: String? = null,
    content: DrawerPanel.() -> Unit = {}
): DrawerPanel =
    register(DrawerPanel(this.drawer, id = id, classes = classes), content)

fun DrawerContent.pfDrawerBody(
    id: String? = null,
    classes: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, classes = classes), content)

fun DrawerPanel.pfDrawerBody(
    id: String? = null,
    classes: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, classes = classes), content)

fun DrawerBody.pfDrawerHead(
    id: String? = null,
    classes: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerHead = register(DrawerHead(this.drawer, id = id, classes = classes), content)

fun DrawerHead.pfDrawerActions(
    id: String? = null,
    classes: String? = null,
    content: DrawerActions.() -> Unit = {}
): DrawerActions = register(DrawerActions(this.drawer, id = id, classes = classes), content)

fun DrawerActions.pfDrawerClose(
    id: String? = null,
    classes: String? = null
): DrawerClose {
    return register(DrawerClose(this.drawer, id = id, classes = classes), {})
}

// ------------------------------------------------------ tag

class Drawer internal constructor(id: String?, classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Drawer, classes)) {

    val expanded = CollapseExpandStore()

    init {
        markAs(ComponentType.Drawer)
        classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

class DrawerActions(internal val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("actions"), classes))

class DrawerBody(internal val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("body"), classes))

class DrawerClose(private val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("close"), classes)) {
    init {
        pfButton("plain".modifier()) {
            pfIcon("times".fas())
            attr("aria-label", "Close drawer panel")
            clicks.map { false } handledBy this@DrawerClose.drawer.expanded.update
        }
    }
}

class DrawerContent(internal val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("content"), classes))

class DrawerHead(internal val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("head"), classes))

class DrawerMain(internal val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("main"), classes))

class DrawerPanel(internal val drawer: Drawer, id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("panel"), classes)) {
    init {
        if (!drawer.domNode.classList.contains("static".modifier())) {
            drawer.expanded.data.map { !it }.bindAttr("hidden")
        }
    }
}

class DrawerSection(id: String?, classes: String?) :
    Div(id = id, baseClass = classes("drawer".component("section"), classes))
