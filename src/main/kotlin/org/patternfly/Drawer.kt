package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TagContext
import kotlinx.browser.document
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain
import org.patternfly.DrawerPanelPosition.BOTTOM
import org.patternfly.DrawerPanelPosition.LEFT
import org.patternfly.DrawerPanelPosition.RIGHT
import org.patternfly.dom.aria
import org.w3c.dom.DOMRect
import org.w3c.dom.Element
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.EventListener
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

/**
 * Creates a [Drawer] component.
 *
 * @param panelPosition controls the position of the [DrawerPanel] component
 * @param resizable whether the [DrawerPanel] is resizable
 * @param inline whether to apply `pf-m-inline` to the [Drawer] component
 * @param static whether to apply `pf-m-static` to the [Drawer] component
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.drawer(
    panelPosition: DrawerPanelPosition = RIGHT,
    resizable: Boolean = false,
    inline: Boolean = false,
    static: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: Drawer.() -> Unit = {}
): Drawer = register(
    Drawer(
        panelPosition = panelPosition,
        resizable = resizable,
        inline = inline,
        static = static,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

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
): DrawerPanel {
    return if (resizable) {
        val drawerPanel = DrawerPanelMain(this, id = id, baseClass = baseClass, job)
        main.register(
            ResizableDrawerPanel(this, id = id, baseClass = baseClass, job),
            {
                it.register(drawerPanel, content)
            }
        )
        return drawerPanel
    } else {
        main.register(
            NonResizableDrawerPanel(this, id = id, baseClass = baseClass, job),
            content
        )
    }
}

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
        drawer,
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
): DrawerBody = register(DrawerBody(drawer, id = id, baseClass = baseClass, job), content)

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
): DrawerBody = register(DrawerBody(drawer, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [DrawerHead] component inside the [DrawerBody] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun DrawerBody.drawerHead(
    id: String? = null,
    baseClass: String? = null,
    content: DrawerHead.() -> Unit = {}
): DrawerHead = register(DrawerHead(drawer, id = id, baseClass = baseClass, job), content)

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
): DrawerAction = register(DrawerAction(drawer, id = id, baseClass = baseClass, job), content)

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
    return register(DrawerClose(drawer, id = id, baseClass = baseClass, job), {})
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
 * 1. [DrawerPanel]: The actual content of the drawer which slides in from left / right / bottom.
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
public class Drawer internal constructor(
    internal val panelPosition: DrawerPanelPosition,
    internal val resizable: Boolean,
    inline: Boolean,
    internal val static: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLDivElement>,
    Div(
        id = id,
        baseClass = classes {
            +ComponentType.Drawer
            +panelPosition.modifier
            +("inline".modifier() `when` inline)
            +("static".modifier() `when` static)
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    internal val main: Div

    /**
     * The increment amount for keyboard drawer resizing, in pixels.
     */
    public var increment: Int = Settings.DRAWER_INCREMENT

    /**
     * The minimum size of a resizable drawer, in pixels. Defaults to the starting width of the drawer.
     */
    public var minSize: Int? = null

    /**
     * The maximum size of a resizable drawer, in pixels. Defaults to the max width of the parent container.
     */
    public var maxSize: Int? = null

    /**
     * Aria described by label for the resizable drawer splitter.
     */
    public var resizeAriaDescribedBy: String =
        "Press space to begin resizing, and use the arrow keys to grow or shrink the panel. " +
            "Press enter or escape to finish resizing."

    /**
     * Aria label for the resizable drawer splitter.
     */
    public var resizeAriaLabel: String = "Resize"

    /**
     * Manages the expanded state of the [Drawer]. Use this property if you want to track the collapse / expand state.
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
) : Div(id = id, baseClass = classes("drawer".component("actions"), baseClass), job, Scope())

/**
 * Component for the content inside the [DrawerContent] and [DrawerPanel] components.
 *
 * Use this class to add content to [DrawerContent]s and [DrawerPanel]s. If used inside [DrawerPanel] you normally add a [DrawerHead], [DrawerAction] and a [DrawerClose] to the first [DrawerBody]. You can use [drawerBodyWithClose] as a shortcut. If you want to have full control you can put the components together on your own though.
 *
 * @sample org.patternfly.sample.DrawerSample.drawerPanels
 */
public class DrawerBody internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("body"), baseClass), job, Scope())

/**
 * Component for the close button inside the [DrawerAction] component.
 */
public class DrawerClose internal constructor(private val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("close"), baseClass), job, Scope()) {

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
) : Div(id = id, baseClass = classes("drawer".component("content"), baseClass), job, Scope())

/**
 * Component for the content left from the [DrawerAction] component in the [DrawerBody] component.
 */
public class DrawerHead internal constructor(internal val drawer: Drawer, id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("head"), baseClass), job, Scope())

/**
 * Component for the actual content of the drawer which slides in from the right edge. Use any number of nested [DrawerBody] components to add the actual content. The first [DrawerBody] should contain a [DrawerHead] with a nested [DrawerAction] and [DrawerClose] component.
 *
 * @sample org.patternfly.sample.DrawerSample.drawerPanels
 */
public interface DrawerPanel : TagContext {
    public val drawer: Drawer
}

internal class NonResizableDrawerPanel internal constructor(
    override val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job,
) : DrawerPanel, Div(
    id = id,
    baseClass = classes("drawer".component("panel"), baseClass),
    job,
    scope = Scope()
) {

    init {
        if (!drawer.static) {
            attr("hidden", drawer.expanded.data.map { !it })
        }
    }
}

internal class ResizableDrawerPanel internal constructor(
    private val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job,
) : Div(
    id = id,
    baseClass = classes {
        +"drawer".component("panel")
        +"resizable".modifier()
        +baseClass
    },
    job,
    scope = Scope()
) {

    private var newSize: Int = 0
    private var resizing: Boolean = false
    private var mouseMoveHandler: EventListener = EventListener {
        if (resizing && domNode.parentElement != null) {
            handleMouseMove(it as MouseEvent, domNode.parentElement!!)
        }
    }
    private var mouseUpHandler: EventListener = EventListener {
        if (resizing) {
            handleMouseUp()
        }
    }

    init {
        div(
            baseClass = classes {
                +"drawer".component("splitter")
                +("vertical".modifier() `when` (drawer.panelPosition != BOTTOM))
            }
        ) {
            aria["orientation"] =
                if (this@ResizableDrawerPanel.drawer.panelPosition == BOTTOM) "horizontal" else "vertical"
            aria["label"] = this@ResizableDrawerPanel.drawer.resizeAriaLabel
            aria["describedby"] = this@ResizableDrawerPanel.drawer.resizeAriaDescribedBy
            attr("role", "separator")
            attr("tabindex", 0)

            domNode.addEventListener(Events.mousedown.name, { this@ResizableDrawerPanel.handleMouseDown(it) })
            domNode.addEventListener(
                Events.keydown.name,
                {
                    with(this@ResizableDrawerPanel) {
                        if (domNode.parentElement != null) {
                            handleKeyDown(it as KeyboardEvent, domNode.parentElement!!)
                        }
                    }
                }
            )

            div(baseClass = "drawer".component("splitter", "handle")) {
                aria["hidden"] = true
            }
        }
    }

    private fun handleMouseDown(e: Event) {
        e.stopPropagation()
        e.preventDefault()
        document.addEventListener(Events.mousemove.name, mouseMoveHandler)
        document.addEventListener(Events.mouseup.name, mouseUpHandler)
        resizing = true
    }

    @Suppress("MagicNumber")
    private fun handleMouseMove(e: MouseEvent, parent: Element) {
        val panelRect = domNode.getBoundingClientRect()
        val (min, max) = minMax(parent.getBoundingClientRect())
        val mousePos = if (drawer.panelPosition == BOTTOM) e.clientY else e.clientX

        newSize = when (drawer.panelPosition) {
            LEFT -> mousePos - panelRect.left.toInt()
            RIGHT -> panelRect.right.toInt() - mousePos
            BOTTOM -> panelRect.bottom.toInt() - mousePos
        }
        if (newSize in min..max) {
            adjustStyle(newSize, max)
        }
    }

    private fun handleMouseUp() {
        resizing = false
        document.removeEventListener(Events.mousemove.name, mouseMoveHandler)
        document.removeEventListener(Events.mouseup.name, mouseUpHandler)
    }

    @Suppress("NestedBlockDepth")
    private fun handleKeyDown(e: KeyboardEvent, parent: Element) {
        if (e.key in RESIZE_KEYS) {
            e.preventDefault()
            if (e.key in RESIZE_START_STOP_KEYS) {
                resizing = e.key == " "
                newSize = if (drawer.panelPosition == BOTTOM) {
                    domNode.getBoundingClientRect().height.toInt()
                } else {
                    domNode.getBoundingClientRect().width.toInt()
                }
            }

            if (resizing) {
                val (min, max) = minMax(parent.getBoundingClientRect())
                val delta = when (e.key) {
                    "ArrowLeft" -> {
                        if (drawer.panelPosition == LEFT) -drawer.increment else drawer.increment
                    }
                    "ArrowRight" -> {
                        if (drawer.panelPosition == LEFT) drawer.increment else -drawer.increment
                    }
                    "ArrowUp" -> {
                        drawer.increment
                    }
                    "ArrowDown" -> {
                        -drawer.increment
                    }
                    else -> 0
                }

                if (newSize + delta in min..max) {
                    newSize += delta
                    adjustStyle(newSize, max)
                }
            }
        } else {
            if (resizing) {
                e.preventDefault()
            }
        }
    }

    private fun minMax(parentRect: DOMRect): Pair<Int, Int> {
        val min = drawer.minSize ?: 0
        val max = drawer.maxSize
            ?: if (drawer.panelPosition == BOTTOM) {
                parentRect.height.toInt()
            } else {
                parentRect.width.toInt()
            }
        return min to max
    }

    @Suppress("MagicNumber")
    private fun adjustStyle(newSize: Int, max: Int) {
        if (drawer.panelPosition == BOTTOM) {
            inlineStyle("overflow-anchor:none")
        }
        val flexBasis = if (drawer.maxSize != null) {
            "${newSize}px"
        } else {
            val percent = ((newSize / max.toDouble()) * 100)
            "$percent%"
        }
        domNode.style.setProperty("--pf-c-drawer__panel--FlexBasis", flexBasis)
    }

    private companion object {
        val RESIZE_KEYS: Array<String> = arrayOf(
            " ",
            "Escape",
            "Enter",
            "ArrowUp",
            "ArrowDown",
            "ArrowLeft",
            "ArrowRight"
        )
        val RESIZE_START_STOP_KEYS = arrayOf(
            " ",
            "Escape",
            "Enter"
        )
    }
}

public class DrawerPanelMain internal constructor(
    override val drawer: Drawer,
    id: String?,
    baseClass: String?,
    job: Job,
) : DrawerPanel, Div(
    id = id,
    baseClass = classes("drawer".component("panel", "main"), baseClass),
    job,
    scope = Scope()
)

/**
 * Component for content above [DrawerContent] and [DrawerPanel].
 */
public class DrawerSection internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("drawer".component("section"), baseClass), job, Scope())
