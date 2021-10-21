package org.patternfly

import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.RenderContext
import org.w3c.dom.HTMLImageElement

// ------------------------------------------------------ factory

/**
 * Creates the [Brand] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param build a lambda expression for setting up the component itself
 */
public fun RenderContext.brand(
    baseClass: String? = null,
    id: String? = null,
    build: Brand.() -> Unit = {}
) {
    Brand().apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * [PatternFly brand](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * A brand is used to place a product logotype on a screen.
 */
public class Brand :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Img, HTMLImageElement> by ElementMixin(),
    WithEvents<HTMLImageElement> by EventMixin() {

    private var alt: String = ""
    private var src: String = ""

    public fun alt(alt: String) {
        this.alt = alt
    }

    public fun src(src: String) {
        this.src = src
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            img(baseClass = classes("brand".component(), baseClass), id = id) {
                alt(alt)
                src(src)
            }
        }
    }
}
