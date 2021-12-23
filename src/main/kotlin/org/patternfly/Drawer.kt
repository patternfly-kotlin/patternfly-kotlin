package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.mountSimple
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Events.keydown
import dev.fritz2.dom.html.Events.mousedown
import dev.fritz2.dom.html.Events.mousemove
import dev.fritz2.dom.html.Events.mouseup
import dev.fritz2.dom.html.Events.touchend
import dev.fritz2.dom.html.Events.touchmove
import dev.fritz2.dom.html.Events.touchstart
import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.document
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariant.plain
import org.patternfly.NotificationStore.job
import org.patternfly.dom.By
import org.patternfly.dom.querySelector
import org.patternfly.dom.removeFromParent
import org.w3c.dom.DOMRect
import org.w3c.dom.HTMLElement
import org.w3c.dom.ParentNode
import org.w3c.dom.Text
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ factory

/**
 * Creates a [Drawer] component.
 *
 * @param variants controls the visual representation of the drawer
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.drawer(
    vararg variants: DrawerVariant,
    baseClass: String? = null,
    id: String? = null,
    context: Drawer.() -> Unit
) {
    Drawer(variants).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [drawer](https://www.patternfly.org/v4/components/drawer/design-guidelines) component.
 *
 * A drawer is a sliding panel that enters from the right edge of the viewport. It can be configured to either overlay content on a page or create a sidebar by pushing that content to the left.
 *
 * A drawer consists of a [primary][DrawerPrimary] and [detail][DrawerDetail] section. Each section can contain multiple content sections. In addition, the [detail][DrawerDetail] section can contain an optional head.
 *
 * @sample org.patternfly.sample.DrawerSample.drawerSetup
 */
@Suppress("TooManyFunctions")
public class Drawer(private val variations: Array<out DrawerVariant>) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithExpandedStore by ExpandedStoreMixin() {

    private val primary: DrawerPrimary = DrawerPrimary()
    private val detail: DrawerDetail = DrawerDetail()

    private val bottom: Boolean = DrawerVariant.bottom in variations
    private val left: Boolean = DrawerVariant.left in variations
    private val resizable: Boolean = DrawerVariant.resizable in variations
    private val static: Boolean = DrawerVariant.static in variations

    private val splitterPosition: RootStore<Int> = storeOf(0)
    private val resizing: RootStore<Boolean> = storeOf(false)
    private var keyResizing: Boolean = false // must not affect CSS classnames!

    private val mouseDownHandler: (Event) -> Unit = ::handleMouseDown
    private val mouseMoveHandler: (Event) -> Unit = ::handleMouseMove
    private val mouseUpHandler: (Event) -> Unit = ::handleMouseUp
    private val touchStartHandler: (Event) -> Unit = ::handleTouchStart
    private val touchMoveHandler: (Event) -> Unit = ::handleTouchMove
    private val touchEndHandler: (Event) -> Unit = ::handleTouchEnd
    private val keyHandler: (Event) -> Unit = ::handleKey

    private lateinit var panelElement: HTMLElement
    private var panelRect: DOMRect? = null

    public fun primary(context: DrawerPrimary.() -> Unit) {
        primary.apply(context)
    }

    public fun detail(context: DrawerDetail.() -> Unit) {
        detail.apply(context)
    }

    // ------------------------------------------------------ render functions

    @Suppress("LongMethod")
    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.Drawer
                    +variations.joinToString(" ") { it.modifier }
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Drawer)
                applyElement(this)
                applyEvents(this)
                classMap(
                    expandedStore.data.combine(resizing.data) { expanded, resizing ->
                        expanded to resizing
                    }.map { (expanded, resizing) ->
                        mapOf(
                            "expanded".modifier() to expanded,
                            "resizing".modifier() to resizing
                        )
                    }
                )

                primary.sections.forEach { content ->
                    renderSection(this, content)
                }
                div(baseClass = "drawer".component("main")) {
                    div(baseClass = classes("drawer".component("content"))) {
                        primary.bodies.forEach { content ->
                            renderBody(this, content)
                        }
                    }
                    div(
                        baseClass = classes {
                            +"drawer".component("panel")
                            +("resizable".modifier() `when` resizable)
                        }
                    ) {
                        panelElement = domNode
                        with(expandedStore) { hideIfCollapsed() }
                        if (resizable) {
                            inlineStyle(
                                splitterPosition.data.filter { it > 0 }.map { position ->
                                    "--pf-c-drawer__panel--md--FlexBasis:${position}px"
                                }
                            )
                            renderSplitter(this)
                            div(baseClass = "drawer".component("panel", "main")) {
                                renderDetail(this)
                            }
                        } else {
                            renderDetail(this)
                        }
                    }
                }
                manageDetailTabIndex(domNode)
                if (static) {
                    expandedStore.expand()
                }
            }
        }
    }

    private fun renderSection(context: RenderContext, section: SubComponent<Div>) {
        with(context) {
            div(
                baseClass = classes("drawer".component("section"), section.baseClass),
                id = section.id
            ) {
                section.context(this)
            }
        }
    }

    private fun renderDetail(context: RenderContext) {
        with(context) {
            detail.head?.let { renderHead(this, it) }
            detail.bodies.forEach { content ->
                renderBody(this, content)
            }
        }
    }

    private fun renderHead(context: RenderContext, head: SubComponent<Div>) {
        with(context) {
            div(
                baseClass = classes("drawer".component("body"), head.baseClass),
                id = head.id
            ) {
                div(baseClass = "drawer".component("head")) {
                    head.context(this)
                    if (!static) {
                        div(baseClass = "drawer".component("actions")) {
                            div(baseClass = "drawer".component("close")) {
                                clickButton(plain) {
                                    icon("times".fas())
                                } handledBy expandedStore.collapse
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderBody(context: RenderContext, body: SubComponent<Div>) {
        with(context) {
            div(
                baseClass = classes("drawer".component("body"), body.baseClass),
                id = body.id
            ) {
                body.context(this)
            }
        }
    }

    private fun renderSplitter(context: RenderContext) {
        with(context) {
            div(
                baseClass = classes {
                    +"drawer".component("splitter")
                    +("vertical".modifier() `when` !bottom)
                }
            ) {
                attr("role", "separator")
                attr("tabindex", 0)
                aria["label"] = "Resize"
                aria["orientation"] = if (bottom) "horizontal" else "vertical"
                aria["describedby"] = "Press space to begin resizing, and use the arrow keys to " +
                    "grow or shrink the panel. Press enter or escape to finish resizing."
                with(domNode) {
                    addEventListener(mousedown.name, mouseDownHandler)
                    addEventListener(keydown.name, keyHandler)
                    addEventListener(touchstart.name, touchStartHandler)
                }
                div(baseClass = "drawer".component("splitter", "handle")) {
                    aria["hidden"] = true
                }
            }
        }
    }

    private fun manageDetailTabIndex(root: ParentNode) {
        val firstElement = root.querySelector(By.classname("drawer".component("head")))?.let { head ->
            if (head.firstChild is Text) {
                val text = head.firstChild!!
                text.removeFromParent()
                val span = document.createElement("span")
                span.appendChild(text)
                head.prepend(span)
                span
            } else {
                head.firstElementChild
            }
        }
        firstElement?.let { element ->
            mountSimple(job, expandedStore.data) { expanded ->
                element.setAttribute("tabindex", if (expanded) "0" else "-1")
            }
        }
    }

    // ------------------------------------------------------ start resizing

    private fun handleMouseDown(event: Event) {
        console.log("start panel content mouse down handler")
        event.stopPropagation()
        event.preventDefault()
        document.addEventListener(mousemove.name, mouseMoveHandler)
        document.addEventListener(mouseup.name, mouseUpHandler)
        resizing.update(true)
        console.log("end   panel content mouse down handler")
    }

    private fun handleTouchStart(event: Event) {
        event.stopPropagation()
        document.addEventListener(touchmove.name, touchMoveHandler, js("{ passive: false }"))
        document.addEventListener(touchend.name, touchEndHandler)
        resizing.update(true)
    }

    // ------------------------------------------------------ resizing in progress

    private fun handleMouseMove(event: Event) {
        val mouseEvent = event.unsafeCast<MouseEvent>()
        val mousePos = if (bottom) mouseEvent.clientY else mouseEvent.clientX
        handleControlMove(event, mousePos)
    }

    private fun handleTouchMove(event: Event) {
        event.preventDefault()
        event.stopImmediatePropagation()
        val touchEvent = event.unsafeCast<TouchEvent>()
        val touchPos = if (bottom) {
            touchEvent.touches.item(0)?.clientY ?: 0
        } else {
            touchEvent.touches.item(0)?.clientX ?: 0
        }
        handleControlMove(event, touchPos)
    }

    // ------------------------------------------------------ end resizing

    private fun handleMouseUp(@Suppress("UNUSED_PARAMETER") event: Event) {
        console.log("start document mouse up handler")
        if (!resizing.current) {
            return
        }
        panelRect = null
        resizing.update(false)
        document.removeEventListener(mousemove.name, mouseMoveHandler)
        document.removeEventListener(mouseup.name, mouseUpHandler)
        console.log("end   document mouse up handler")
    }

    private fun handleTouchEnd(event: Event) {
        event.stopPropagation()
        if (!resizing.current) {
            return
        }
        panelRect = null
        resizing.update(false)
        document.removeEventListener(touchmove.name, touchMoveHandler)
        document.removeEventListener(touchend.name, touchEndHandler)
    }

    // ------------------------------------------------------ do resizing

    private fun handleControlMove(event: Event, controlPosition: Int) {
        event.stopPropagation()
        if (!resizing.current) {
            return
        }
        val rect = panelRect ?: panelElement.getBoundingClientRect()
        splitterPosition.update(
            if (bottom) {
                rect.bottom.toInt() - controlPosition
            } else if (left) {
                controlPosition - rect.left.toInt()
            } else { // right
                rect.right.toInt() - controlPosition
            }
        )
    }

    @Suppress("ComplexMethod")
    private fun handleKey(event: Event) {
        val key = event.unsafeCast<KeyboardEvent>().key
        if (key !in RESIZE_KEYS) {
            if (keyResizing) {
                event.preventDefault()
            }
            return
        }
        event.preventDefault()
        if (key in RESIZE_START_STOP_KEYS) {
            keyResizing = key == " "
        }
        if (keyResizing) {
            val multiplier = if (event.unsafeCast<KeyboardEvent>().shiftKey) MULTIPLIER else 1
            val rect = panelRect ?: panelElement.getBoundingClientRect()
            val delta = when (key) {
                "ArrowRight" -> if (left) INCREMENT else -INCREMENT
                "ArrowLeft" -> if (left) -INCREMENT else INCREMENT
                "ArrowUp" -> INCREMENT
                "ArrowDown" -> -INCREMENT
                else -> 0
            }
            splitterPosition.update(
                if (bottom) {
                    rect.height.toInt() + (multiplier * delta)
                } else {
                    rect.width.toInt() + (multiplier * delta)
                }
            )
        }
    }

    private companion object {
        const val INCREMENT: Int = 5
        const val MULTIPLIER: Int = 10
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

/**
 * Visual modifiers for [Drawer]s.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class DrawerVariant(internal val modifier: String) {
    bottom("panel-bottom".modifier()),
    `inline`("inline".modifier()),
    left("panel-left".modifier()),
    resizable(""),
    static("static".modifier()),
}

/**
 * The primary container of a [drawer][Drawer] component which is always visible.
 *
 * The primary container does not contain content on its own. All content must go into one or multiple [content] containers.
 */
public class DrawerPrimary {

    private val contents: MutableList<Pair<Boolean, SubComponent<Div>>> = mutableListOf()

    internal val sections: List<SubComponent<Div>>
        get() = contents.filter { it.first }.map { it.second }

    internal val bodies: List<SubComponent<Div>>
        get() = contents.filter { !it.first }.map { it.second }

    /**
     * Adds a content container.
     */
    public fun content(
        fullWidth: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        contents.add(fullWidth to SubComponent(baseClass, id, context))
    }
}

/**
 * The detail container of a [drawer][Drawer] component which slides in from the edge of the viewport.
 *
 *  The detail container does not contain content on its own. All content must go into one optional [head] and one or multiple [content] containers. If present, the [head] contains a close button to hide the detail section.
 */
public class DrawerDetail {

    internal var head: SubComponent<Div>? = null
    internal val bodies: MutableList<SubComponent<Div>> = mutableListOf()

    /**
     * Sets the head section.
     */
    public fun head(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        this.head = SubComponent(baseClass, id, context)
    }

    /**
     * Adds a content container.
     */
    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        bodies.add(SubComponent(baseClass, id, context))
    }
}
