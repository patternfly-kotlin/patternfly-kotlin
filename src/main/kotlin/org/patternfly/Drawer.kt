package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfDrawer(
    id: String? = null,
    baseClass: String? = null,
    content: Drawer.() -> Unit = {}
): Drawer = register(Drawer(id = id, baseClass = baseClass, job), content)

public fun Drawer.pfDrawerSection(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerSection.() -> Unit = {}
): DrawerSection = register(DrawerSection(id = id, baseClass = baseClass, job), content)

public fun Drawer.pfDrawerMain(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerMain.() -> Unit = {}
): DrawerMain = register(DrawerMain(this, id = id, baseClass = baseClass, job), content)

public fun DrawerMain.pfDrawerContent(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerContent.() -> Unit = {}
): DrawerContent =
    register(DrawerContent(this.drawer, id = id, baseClass = baseClass, job), content)

public fun DrawerMain.pfDrawerPanel(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerPanel.() -> Unit = {}
): DrawerPanel =
    register(DrawerPanel(this.drawer, id = id, baseClass = baseClass, job), content)

public fun DrawerContent.pfDrawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass, job), content)

public fun DrawerPanel.pfDrawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass, job), content)

public fun DrawerBody.pfDrawerHead(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerHead = register(DrawerHead(this.drawer, id = id, baseClass = baseClass, job), content)

public fun DrawerHead.pfDrawerActions(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerActions.() -> Unit = {}
): DrawerActions = register(DrawerActions(this.drawer, id = id, baseClass = baseClass, job), content)

public fun DrawerActions.pfDrawerClose(
    id: String? = null,
    baseClass: String? = null
): DrawerClose {
    return register(DrawerClose(this.drawer, id = id, baseClass = baseClass, job), {})
}

// ------------------------------------------------------ tag

public class
Drawer internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Drawer, baseClass), job) {

    public val expanded: CollapseExpandStore = CollapseExpandStore()

    init {
        markAs(ComponentType.Drawer)
        classMap(expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
    }
}

public class DrawerActions internal constructor(
    internal val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job
) :
    Div(id = id, baseClass = classes("drawer".component("actions"), baseClass), job)

public class DrawerBody internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("body"), baseClass), job)

public class DrawerClose internal constructor(private val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("close"), baseClass), job) {
    init {
        button("plain".modifier()) {
            icon("times".fas())
            attr("aria-label", "Close drawer panel")
            clicks.map { false } handledBy this@DrawerClose.drawer.expanded.update
        }
    }
}

public class DrawerContent internal constructor(
    internal val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("drawer".component("content"), baseClass), job)

public class DrawerHead internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("head"), baseClass), job)

public class DrawerMain internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("main"), baseClass), job)

public class DrawerPanel internal constructor(
    internal val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("drawer".component("panel"), baseClass), job) {
    init {
        if (!drawer.domNode.classList.contains("static".modifier())) {
            attr("hidden", drawer.expanded.data.map { !it })
        }
    }
}

public class DrawerSection internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("section"), baseClass), job)
