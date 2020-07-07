package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun HtmlElements.pfDrawer(content: Drawer.() -> Unit = {}): Drawer =
    register(Drawer(), content)

fun Drawer.pfDrawerSection(content: DrawerSection.() -> Unit = {}): DrawerSection =
    register(DrawerSection(this.expanded), content)

fun Drawer.pfDrawerMain(content: DrawerMain.() -> Unit = {}): DrawerMain =
        register(DrawerMain(this.expanded), content)

fun DrawerMain.pfDrawerContent(content: DrawerContent.() -> Unit = {}): DrawerContent =
    register(DrawerContent(this.store), content)

fun DrawerMain.pfDrawerPanel(content: DrawerPanel.() -> Unit = {}): DrawerPanel =
    register(DrawerPanel(this.store), content)

fun DrawerContent.pfDrawerBody(content: DrawerBody.() -> Unit = {}): DrawerBody =
    register(DrawerBody(this.store), content)

fun DrawerPanel.pfDrawerBody(content: DrawerBody.() -> Unit = {}): DrawerBody =
    register(DrawerBody(this.store), content)

fun DrawerBody.pfDrawerHead(content: DrawerHead.() -> Unit = {}): DrawerHead =
    register(DrawerHead(this.store), content)

fun DrawerHead.pfDrawerActions(content: DrawerActions.() -> Unit = {}): DrawerActions =
    register(DrawerActions(this.store), content)

fun DrawerActions.pfDrawerClose(): DrawerClose =
    register(DrawerClose(this.store), {})

// ------------------------------------------------------ tag

class Drawer : PatternFlyTag<HTMLDivElement>(ComponentType.Drawer, "div", "drawer".component()) {
    val expanded = ExpandedStore()
    init {
        classMap = expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

class DrawerActions(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("actions"))

class DrawerBody(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("body"))

class DrawerClose(private val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("close")) {
    init {
        pfPlainButton(iconClass = "times".fas()) {
            attr("tabIndex", "-1")
            attr("aria-label", "Close drawer panel")
            clicks.map { false } handledBy this@DrawerClose.store.update
        }
    }
}

class DrawerContent(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("content"))

class DrawerHead(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("head"))

class DrawerMain(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("main"))

class DrawerPanel(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("panel")) {
    init {
        store.data.bindAttr("hidden", domNode)
    }
}

class DrawerSection(internal val store: ExpandedStore) :
    Tag<HTMLDivElement>("div", baseClass = "drawer".component("section"))

// ------------------------------------------------------ store

class ExpandedStore : RootStore<Boolean>(false)
