package org.patternfly

import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.map
import org.patternfly.Modifier.plain
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDrawer(classes: String? = null, content: Drawer.() -> Unit = {}): Drawer =
    register(Drawer(classes), content)

fun HtmlElements.pfDrawer(modifier: Modifier, content: Drawer.() -> Unit = {}): Drawer =
    register(Drawer(modifier.value), content)

fun Drawer.pfDrawerSection(classes: String? = null, content: DrawerSection.() -> Unit = {}): DrawerSection =
    register(DrawerSection(this, classes), content)

fun Drawer.pfDrawerSection(modifier: Modifier, content: DrawerSection.() -> Unit = {}): DrawerSection =
    register(DrawerSection(this, modifier.value), content)

fun Drawer.pfDrawerMain(classes: String? = null, content: DrawerMain.() -> Unit = {}): DrawerMain =
    register(DrawerMain(this, classes), content)

fun Drawer.pfDrawerMain(modifier: Modifier, content: DrawerMain.() -> Unit = {}): DrawerMain =
    register(DrawerMain(this, modifier.value), content)

fun DrawerMain.pfDrawerContent(classes: String? = null, content: DrawerContent.() -> Unit = {}): DrawerContent =
    register(DrawerContent(this.drawer, classes), content)

fun DrawerMain.pfDrawerContent(modifier: Modifier, content: DrawerContent.() -> Unit = {}): DrawerContent =
    register(DrawerContent(this.drawer, modifier.value), content)

fun DrawerMain.pfDrawerPanel(classes: String? = null, content: DrawerPanel.() -> Unit = {}): DrawerPanel =
    register(DrawerPanel(this.drawer, classes), content)

fun DrawerMain.pfDrawerPanel(modifier: Modifier, content: DrawerPanel.() -> Unit = {}): DrawerPanel =
    register(DrawerPanel(this.drawer, modifier.value), content)

fun DrawerContent.pfDrawerBody(classes: String? = null, content: DrawerBody.() -> Unit = {}): DrawerBody =
    register(DrawerBody(this.drawer, classes), content)

fun DrawerContent.pfDrawerBody(modifier: Modifier, content: DrawerBody.() -> Unit = {}): DrawerBody =
    register(DrawerBody(this.drawer, modifier.value), content)

fun DrawerPanel.pfDrawerBody(classes: String? = null, content: DrawerBody.() -> Unit = {}): DrawerBody =
    register(DrawerBody(this.drawer, classes), content)

fun DrawerPanel.pfDrawerBody(modifier: Modifier, content: DrawerBody.() -> Unit = {}): DrawerBody =
    register(DrawerBody(this.drawer, modifier.value), content)

fun DrawerBody.pfDrawerHead(classes: String? = null, content: DrawerHead.() -> Unit = {}): DrawerHead =
    register(DrawerHead(this.drawer, classes), content)

fun DrawerBody.pfDrawerHead(modifier: Modifier, content: DrawerHead.() -> Unit = {}): DrawerHead =
    register(DrawerHead(this.drawer, modifier.value), content)

fun DrawerHead.pfDrawerActions(classes: String? = null, content: DrawerActions.() -> Unit = {}): DrawerActions =
    register(DrawerActions(this.drawer, classes), content)

fun DrawerHead.pfDrawerActions(modifier: Modifier, content: DrawerActions.() -> Unit = {}): DrawerActions =
    register(DrawerActions(this.drawer, modifier.value), content)

fun DrawerActions.pfDrawerClose(classes: String? = null): DrawerClose =
    register(DrawerClose(this.drawer, classes), {})

fun DrawerActions.pfDrawerClose(modifier: Modifier): DrawerClose =
    register(DrawerClose(this.drawer, modifier.value), {})

// ------------------------------------------------------ tag

class Drawer internal constructor(classes: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes(ComponentType.Drawer, classes)) {

    val expanded = CollapseExpandStore()

    init {
        markAs(ComponentType.Drawer)
        classMap = expanded.data.map { expanded -> mapOf(Modifier.expanded.value to expanded) }
    }
}

class DrawerActions(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("actions"), classes))

class DrawerBody(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("body"), classes))

class DrawerClose(private val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("close"), classes)) {
    init {
        pfButton(plain) {
            pfIcon("times".fas())
            attr("aria-label", "Close drawer panel")
            clicks.map { false } handledBy this@DrawerClose.drawer.expanded.update
        }
    }
}

class DrawerContent(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("content"), classes))

class DrawerHead(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("head"), classes))

class DrawerMain(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("main"), classes))

class DrawerPanel(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("panel"), classes)) {
    init {
        if (!drawer.domNode.classList.contains("static".modifier())) {
            drawer.expanded.data.map { !it }.bindAttr("hidden")
        }
    }
}

class DrawerSection(internal val drawer: Drawer, classes: String?) :
    Div(baseClass = classes("drawer".component("section"), classes))
