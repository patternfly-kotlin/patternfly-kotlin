package org.patternfly

import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.Job
import org.patternfly.Size.XL
import org.patternfly.dom.aria
import org.w3c.dom.HTMLSpanElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Spinner] component.
 *
 * @param size the size of the spinner. Supported sizes are [Size.SM], [Size.MD], [Size.LG] and [Size.XL].
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.spinner(
    size: Size = XL,
    id: String? = null,
    baseClass: String? = null,
    content: Spinner.() -> Unit = {}
): Spinner = register(Spinner(size, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [spinner](https://www.patternfly.org/v4/components/spinner/design-guidelines) component.
 *
 * A spinner is used to indicate to users that an action is in progress.
 *
 * @sample org.patternfly.sample.SpinnerSample.spinner
 */
public class Spinner internal constructor(size: Size, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLSpanElement>,
    Span(
        id = id,
        baseClass = classes {
            +ComponentType.Spinner
            +size.modifier
            +baseClass
        },
        job
    ) {

    init {
        markAs(ComponentType.Spinner)
        attr("role", "progressbar")
        aria["valuetext"] = "Loading..."

        span(baseClass = "spinner".component("clipper")) {}
        span(baseClass = "spinner".component("lead-ball")) {}
        span(baseClass = "spinner".component("tail-ball")) {}
    }
}
