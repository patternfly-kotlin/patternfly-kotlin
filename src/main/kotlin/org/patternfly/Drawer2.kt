package org.patternfly

import dev.fritz2.binding.mountSimple
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.document
import org.patternfly.ButtonVariant.plain
import org.patternfly.NotificationStore.job
import org.patternfly.dom.By
import org.patternfly.dom.querySelector
import org.patternfly.dom.removeFromParent
import org.w3c.dom.ParentNode
import org.w3c.dom.Text

// ------------------------------------------------------ factory

/**
 * Creates a [Drawer2] component.
 *
 * @param variants controls the visual representation of the drawer
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.drawer2(
    vararg variants: DrawerVariant,
    baseClass: String? = null,
    id: String? = null,
    context: Drawer2.() -> Unit
) {
    Drawer2(variants).apply(context).render(this, baseClass, id)
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
public class Drawer2(private val variations: Array<out DrawerVariant>) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithExpandedStore by ExpandedStoreMixin() {

    private val primary: DrawerPrimary = DrawerPrimary()
    private val detail: DrawerDetail = DrawerDetail()
    private val bottom: Boolean = DrawerVariant.bottom in variations
    private val resizable: Boolean = DrawerVariant.resizable in variations
    private val static: Boolean = DrawerVariant.static in variations

    public fun primary(context: DrawerPrimary.() -> Unit) {
        primary.apply(context)
    }

    public fun detail(context: DrawerDetail.() -> Unit) {
        detail.apply(context)
    }

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
                with(expandedStore) { toggleExpanded() }

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
                        with(expandedStore) { hideIfCollapsed() }
                        if (resizable) {
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
                aria["describedby"] =
                    """Press space to begin resizing, and use the arrow keys to grow or shrink the panel. 
                        |Press enter or escape to finish resizing.""".trimMargin()
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
}

/**
 * Visual modifiers for [Drawer2]s.
 */
@Suppress("EnumEntryName", "EnumNaming")
public enum class DrawerVariant(internal val modifier: String) {
    bottom("panel-bottom".modifier()),
    `inline`("inline".modifier()),
    left("panel-left".modifier()),
    resizable("".modifier()),
    static("static".modifier()),
}

/**
 * The primary part of the [drawer][Drawer2] component which is always visible.
 *
 * The primary section does not contain content on its own. All content must go into one or several [content] sections.
 */
public class DrawerPrimary {

    private val contents: MutableList<Pair<Boolean, SubComponent<Div>>> = mutableListOf()

    internal val sections: List<SubComponent<Div>>
        get() = contents.filter { it.first }.map { it.second }

    internal val bodies: List<SubComponent<Div>>
        get() = contents.filter { !it.first }.map { it.second }

    /**
     * Adds a content section.
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
 * The detail part of the [drawer][Drawer2] component which slides in from the edge of the viewport.
 *
 *  The detail section does not contain content on its own. All content must go into one optional [head] and one or several [content] sections. The [head] contains a close button to hide the detail section.
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
     * Adds a content section.
     */
    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: Div.() -> Unit
    ) {
        bodies.add(SubComponent(baseClass, id, context))
    }
}
