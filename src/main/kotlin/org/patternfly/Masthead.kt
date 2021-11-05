package org.patternfly

import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonVariation.plain

public class Masthead : PatternFlyComponent<Unit> {

    private var toggle: Boolean = false
    private var brandHref: String = "#"
    private var brandSrc: String = ""
    private var brandAlt: String = ""
    private var brand: SubComponent<Brand>? = null
    private var content: SubComponent<RenderContext>? = null

    public fun toggle() {
        this.toggle = true
    }

    public fun brand(
        href: String = "#",
        src: String = "",
        alt: String = "",
        baseClass: String? = null,
        id: String? = null,
        context: Brand.() -> Unit = {}
    ) {
        this.brandHref = href
        this.brandSrc = src
        this.brandAlt = alt
        this.brand = SubComponent(baseClass, id, context)
    }

    public fun content(
        baseClass: String? = null,
        id: String? = null,
        context: RenderContext.() -> Unit = {}
    ) {
        this.content = SubComponent(baseClass, id, context)
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            header(
                baseClass = classes(ComponentType.Masthead, baseClass),
                id = id,
                scope = {
                    set(Scopes.MASTHEAD, true)
                }
            ) {
                markAs(ComponentType.Masthead)
                if (this@Masthead.toggle) {
                    div(baseClass = "masthead".component("toggle")) {
                        scope[Scopes.SIDEBAR_STORE]?.let { sidebarStore ->
                            clickButton(plain) {
                                aria["label"] = "Global navigation"
                                aria["expanded"] = sidebarStore.data.map { it.expanded.toString() }
                                icon("bars".fas())
                            } handledBy sidebarStore.toggle
                        }
                    }
                }
                brand?.let { component ->
                    div(baseClass = "masthead".component("main")) {
                        a(baseClass = "masthead".component("brand")) {
                            href(brandHref)
                            attr("tabindex", "0")
                            brand(
                                src = brandSrc,
                                alt = brandAlt,
                                baseClass = component.baseClass,
                                id = component.id
                            ) {
                                component.context(this)
                            }
                        }
                    }
                }
                content?.let { component ->
                    div(
                        baseClass = classes("masthead".component("content"), component.baseClass),
                        id = component.id
                    ) {
                        component.context(this)
                    }
                }
            }
        }
    }
}
