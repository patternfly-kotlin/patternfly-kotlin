package org.patternfly

import dev.fritz2.dom.html.RenderContext

// ------------------------------------------------------ factory

/**
 * Creates the [Brand] component.
 *
 * @param src the source of the brand image
 * @param alt the alternative text of the brand image
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.brand(
    src: String = "",
    alt: String = "",
    baseClass: String? = null,
    id: String? = null,
    context: Brand.() -> Unit = {}
) {
    Brand(src, alt).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * [PatternFly brand](https://www.patternfly.org/v4/components/page/design-guidelines) component.
 *
 * A brand is used to place a product logotype on a screen.
 */
public class Brand internal constructor(private var alt: String, private var src: String) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    public fun alt(alt: String) {
        this.alt = alt
    }

    public fun src(src: String) {
        this.src = src
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            img(baseClass = classes("brand".component(), baseClass), id = id) {
                markAs(ComponentType.Brand)
                applyElement(this)
                applyEvents(this)

                alt(alt)
                src(src)
            }
        }
    }
}
