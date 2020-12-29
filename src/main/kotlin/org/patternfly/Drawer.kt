package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.dom.aria
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Drawer] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.drawer(
    id: String? = null,
    baseClass: String? = null,
    content: Drawer.() -> Unit = {}
): Drawer = register(Drawer(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerSection] component inside a [Drawer] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Drawer.drawerSection(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerSection.() -> Unit = {}
): DrawerSection {
    val section = DrawerSection(id = id, baseClass = baseClass, job).apply {
        content(this)
    }
    domNode.prepend(section.domNode)
    return section
}

/**
 * Creates a [DrawerContent] component inside a [Drawer] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Drawer.drawerContent(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerContent.() -> Unit = {}
): DrawerContent =
    main.register(DrawerContent(this, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerPanel] component inside a [Drawer] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun Drawer.drawerPanel(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerPanel.() -> Unit = {}
): DrawerPanel =
    main.register(DrawerPanel(this, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerBody] component inside the [DrawerPanel] component which contains a [DrawerHead], [DrawerAction] and [DrawerClose] component. Use this as a shortcut if you don't need any customization of the built components.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the [DrawerHead]
 *
 * @sample org.patternfly.sample.DrawerSample.drawerPanels
 */
public fun DrawerPanel.drawerBodyWithClose(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerBody = register(
    DrawerBody(
        this.drawer,
        id = id,
        baseClass = baseClass,
        job
    ),
    {
        it.drawerHead {
            content(this)
            drawerAction {
                drawerClose()
            }
        }
    }
)

/**
 * Creates a [DrawerBody] component inside the [DrawerPanel] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun DrawerPanel.drawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerBody] component inside the [DrawerContent] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun DrawerContent.drawerBody(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerBody.() -> Unit = {}
): DrawerBody = register(DrawerBody(this.drawer, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerHead] component iside the [DrawerBody] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun DrawerBody.drawerHead(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerHead = register(DrawerHead(this.drawer, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerAction] component inside the [DrawerHead] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun DrawerHead.drawerAction(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerAction.() -> Unit = {}
): DrawerAction = register(DrawerAction(this.drawer, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerClose] component inside the [DrawerAction] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 */
public fun DrawerAction.drawerClose(
    id: String? = null,
    baseClass: String? = null
): DrawerClose {
    return register(DrawerClose(this.drawer, id = id, baseClass = baseClass, job), {})
}

// ------------------------------------------------------ tag

/**
 * PatternFly [drawer](https://www.patternfly.org/v4/components/drawer/design-guidelines) component.
 *
 * A drawer is a sliding panel that enters from the right edge of the viewport. It can be configured to either overlay content on a page or create a sidebar by pushing that content to the left.
 *
 * A drawer contains three main components:
 *
 * 1. [DrawerSection]: An optional component above [DrawerContent] and [DrawerPanel].
 * 1. [DrawerContent]: The normal content which is overlayed or pushed aside by the [DrawerPanel].
 * 1. [DrawerPanel]: The actual content of the drawer which slides in from the right edge.
 *
 * The [DrawerContent] and [DrawerPanel] components contain [DrawerBody] components to add the actual content. The first [DrawerBody] component inside the [DrawerPanel] component normally contains a [DrawerHead] component with a nested [DrawerAction] and [DrawerClose] component. You can use [drawerBodyWithClose] as a shortcut. If you want to have full control you can put the components together on your own though.
 *
 * ```
 * ┏━━━━━━━━━━━━━━━ drawer: Drawer ━━━━━━━━━━━━━━━┓
 * ┃ ┌──────────────────────────────────────────┐ ┃
 * ┃ │       drawerSection: DrawerSection       │ ┃
 * ┃ └──────────────────────────────────────────┘ ┃
 * ┃                                              ┃
 * ┃ ┌────── drawerContent: DrawerContent ──────┐ ┃
 * ┃ │ ┌──────────────────────────────────────┐ │ ┃
 * ┃ │ │        drawerBody: DrawerBody        │ │ ┃
 * ┃ │ └──────────────────────────────────────┘ │ ┃
 * ┃ └──────────────────────────────────────────┘ ┃
 * ┃                                              ┃
 * ┃ ┌──────── drawerPanel: DrawerPanel ────────┐ ┃
 * ┃ │                                          │ ┃
 * ┃ │ ┌─────── drawerBody: DrawerBody ───────┐ │ ┃
 * ┃ │ │                                      │ │ ┃
 * ┃ │ │ ┌───── drawerHead: DrawerHead ─────┐ │ │ ┃
 * ┃ │ │ │                                  │ │ │ ┃
 * ┃ │ │ │ ┌─ drawerAction: DrawerAction ─┐ │ │ │ ┃
 * ┃ │ │ │ │ ┌──────────────────────────┐ │ │ │ │ ┃
 * ┃ │ │ │ │ │ drawerClose: DrawerClose │ │ │ │ │ ┃
 * ┃ │ │ │ │ └──────────────────────────┘ │ │ │ │ ┃
 * ┃ │ │ │ └──────────────────────────────┘ │ │ │ ┃
 * ┃ │ │ └──────────────────────────────────┘ │ │ ┃
 * ┃ │ └──────────────────────────────────────┘ │ ┃
 * ┃ └──────────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 *
 * @sample org.patternfly.sample.DrawerSample.drawerSetup
 */
public class Drawer internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.Drawer, baseClass), job) {

    internal val main: Div

    /**
     * Manages the Manages the expanded state of the [Drawer]. Use this property if you want to track the collapse / expand state.
     *
     * @sample org.patternfly.sample.DrawerSample.expanded
     */
    public val expanded: ExpandedStore = ExpandedStore()

    init {
        markAs(ComponentType.Drawer)
        classMap(expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
        main = div(baseClass = classes("drawer".component("main"))) { }
    }
}

/**
 * Component for the actions inside the [DrawerHead] component.
 */
public class DrawerAction internal constructor(
    internal val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("drawer".component("actions"), baseClass), job)

/**
 * Component for the content inside the [DrawerContent] and [DrawerPanel] components.
 *
 * Use this class to add content to [DrawerContent]s and [DrawerPanel]s. If used for the [DrawerPanel] you normally add a [DrawerHead], [DrawerAction] and a [DrawerClose] to the [DrawerBody]. You can use [drawerBodyWithClose] as a shortcut. If you want to have full control you can put the components together on your own though.
 *
 * @sample org.patternfly.sample.DrawerSample.drawerPanels
 */
public class DrawerBody internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("body"), baseClass), job)

/**
 * Component for the close button inside the [DrawerAction] component.
 */
public class DrawerClose internal constructor(private val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("close"), baseClass), job) {

    init {
        pushButton(plain) {
            icon("times".fas())
            aria["label"] = "Close drawer panel"
            clicks.map { false } handledBy this@DrawerClose.drawer.expanded.update
        }
    }
}

/**
 * Component for the normal content which is overlayed or pushed aside by the [DrawerPanel] component. Use any number of nested [DrawerBody] components to add the actual content.
 *
 * @sample org.patternfly.sample.DrawerSample.drawerContents
 */
public class DrawerContent internal constructor(
    internal val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("drawer".component("content"), baseClass), job)

/**
 * Component for the content left from the [DrawerAction] component in the [DrawerBody] component.
 */
public class DrawerHead internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("head"), baseClass), job)

/**
 * Component for the actual content of the drawer which slides in from the right edge. Use any number of nested [DrawerBody] components to add the actual content. The first [DrawerBody] should contain a [DrawerHead] with a nested [DrawerAction] and [DrawerClose] component.
 *
 * @sample org.patternfly.sample.DrawerSample.drawerPanels
 */
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

/**
 * Component for content above [DrawerContent] and [DrawerPanel].
 */
public class DrawerSection internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("section"), baseClass), job)
