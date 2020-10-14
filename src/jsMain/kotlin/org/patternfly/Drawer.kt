package org.patternfly

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun HtmlElements.pfDrawer(
    id: String? = null,
    baseClass: String? = null,
    content: Drawer.() -> Unit = {}
): Drawer = register(Drawer(id = id, baseClass = baseClass), content)

public fun Drawer.pfDrawerSection(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerSection.() -> Unit = {}
): DrawerSection = register(DrawerSection(id = id, baseClass = baseClass), content)

public fun Drawer.pfDrawerMain(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerMain.() -> Unit = {}
): DrawerMain = register(DrawerMain(this, id = id, baseClass = baseClass), content)

public fun DrawerMain.pfDrawerContent(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerContent.() -> Unit = {}
): DrawerContent =
    register(DrawerContent(this.drawer, id = id, baseClass = baseClass), content)

public fun DrawerMain.pfDrawerPanel(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerPanel.() -> Unit = {}
): DrawerPanel =
    register(DrawerPanel(this.drawer, id = id, baseClass = baseClass), content)

public fun DrawerContent.pfDrawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass), content)

public fun DrawerPanel.pfDrawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass), content)

public fun DrawerBody.pfDrawerHead(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerHead = register(DrawerHead(this.drawer, id = id, baseClass = baseClass), content)

public fun DrawerHead.pfDrawerActions(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerActions.() -> Unit = {}
): DrawerActions = register(DrawerActions(this.drawer, id = id, baseClass = baseClass), content)

public fun DrawerActions.pfDrawerClose(
    id: String? = null,
    baseClass: String? = null
): DrawerClose {
    return register(DrawerClose(this.drawer, id = id, baseClass = baseClass), {})
}

// ------------------------------------------------------ tag

public class Drawer internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Drawer, baseClass)) {

    public val expanded: CollapseExpandStore = CollapseExpandStore()

    init {
        markAs(ComponentType.Drawer)
        classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

public class DrawerActions internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("actions"), baseClass))

public class DrawerBody internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("body"), baseClass))

public class DrawerClose internal constructor(private val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("close"), baseClass)) {
    init {
        pfButton("plain".modifier()) {
            pfIcon("times".fas())
            attr("aria-label", "Close drawer panel")
            clicks.map { false } handledBy this@DrawerClose.drawer.expanded.update
        }
    }
}

public class DrawerContent internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("content"), baseClass))

public class DrawerHead internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("head"), baseClass))

public class DrawerMain internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("main"), baseClass))

public class DrawerPanel internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("panel"), baseClass)) {
    init {
        if (!drawer.domNode.classList.contains("static".modifier())) {
            drawer.expanded.data.map { !it }.bindAttr("hidden")
        }
    }
}

public class DrawerSection internal constructor(id: String?, baseClass: String?) :
    Div(id = id, baseClass = classes("drawer".component("section"), baseClass))
