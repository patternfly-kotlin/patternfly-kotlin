package org.patternfly

import dev.fritz2.dom.html.RenderContext

// ------------------------------------------------------ factory

/**
 * Creates a [Spinner] component.
 *
 * @param size the size of the spinner. Supported sizes are [Size.SM], [Size.MD], [Size.LG] and [Size.XL].
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.spinner(
    size: Size = Size.MD,
    baseClass: String? = null,
    id: String? = null,
    context: Spinner.() -> Unit = {}
) {
    Spinner(size).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [spinner](https://www.patternfly.org/v4/components/spinner/design-guidelines) component.
 *
 * A spinner is used to indicate to users that an action is in progress.
 *
 * @sample org.patternfly.sample.SpinnerSample.spinner
 */
public class Spinner internal constructor(private val size: Size) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            span(
                baseClass = classes {
                    +"spinner".component()
                    +size.modifier
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Spinner)
                attr("role", "progressbar")
                aria["valuetext"] = "Loading..."
                applyElement(this)
                applyEvents(this)

                span(baseClass = "spinner".component("clipper")) {}
                span(baseClass = "spinner".component("lead-ball")) {}
                span(baseClass = "spinner".component("tail-ball")) {}
            }
        }
    }
}
