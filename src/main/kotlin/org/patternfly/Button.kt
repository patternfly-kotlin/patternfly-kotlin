package org.patternfly

import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.A
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.events.MouseEvent

// ------------------------------------------------------ dsl

/**
 * Creates a [PushButton] component. This component uses a `<button/>` element.
 *
 * @param variation variations to control the visual representation of the button
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.pushButton(
    vararg variation: ButtonVariation,
    id: String? = null,
    baseClass: String? = null,
    content: PushButton.() -> Unit = {}
): PushButton = register(PushButton(variation, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [PushButton] component and returns a [Listener] (basically a [Flow]) in order to combine the button declaration directly to a fitting _handler_.
 *
 * @param variation variations to control the visual representation of the button
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample ButtonSamples.clickButton
 */
public fun RenderContext.clickButton(
    vararg variation: ButtonVariation,
    id: String? = null,
    baseClass: String? = null,
    content: PushButton.() -> Unit = {}
): Listener<MouseEvent, HTMLButtonElement> {
    var clickEvents: Listener<MouseEvent, HTMLButtonElement>? = null
    pushButton(*variation, id = id, baseClass = baseClass) {
        content(this)
        clickEvents = clicks
    }
    return clickEvents!!
}

/**
 * Creates a [LinkButton] component. This component uses an `<a/>` element.
 *
 * @param variation variations to control the visual representation of the button
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.linkButton(
    vararg variation: ButtonVariation,
    id: String? = null,
    baseClass: String? = null,
    content: LinkButton.() -> Unit = {}
): LinkButton = register(LinkButton(variation, id = id, baseClass = baseClass, job), content)

/**
 * Adds an [Icon] to a [PushButton] or [LinkButton]. Use this function if you also want to add other elements like text to the button. This function adds the icons inside a container that controls the margin between the icon and the other elements (like the text).
 *
 * If you only want to add an icon, you don't have to use this function.
 *
 * @param iconPosition the position of the icon
 * @param id the ID of the icon element
 * @param baseClass optional CSS class that should be applied to the icon element
 * @param content a lambda expression for setting up the icon component
 *
 * @sample ButtonSamples.buttonIcon
 * @sample ButtonSamples.justIcon
 */
public fun ButtonLike.buttonIcon(
    iconPosition: IconPosition,
    iconClass: String,
    id: String? = null,
    baseClass: String? = null,
    content: Icon.() -> Unit = {},
): ButtonIcon = register(ButtonIcon(iconPosition, iconClass, id, baseClass, job, content), {})

// ------------------------------------------------------ tag

/** Marker interface for [PushButton] and [LinkButton]s. */
public interface ButtonLike : RenderContext

/**
 * PatternFly [push button](https://www.patternfly.org/v4/components/button/design-guidelines) component based on a `<button/>` element.
 *
 * A button is a box area or text that communicates and triggers user actions when clicked or selected.
 *
 * @sample ButtonSamples.pushButton
 */
public class PushButton internal constructor(
    variations: Array<out ButtonVariation>,
    id: String?,
    baseClass: String?,
    job: Job
) : Button(id, classes {
    +ComponentType.Button
    +variations.joinToString(" ") { it.modifier }
    +baseClass
}, job), ButtonLike, PatternFlyComponent<HTMLButtonElement> {

    init {
        markAs(ComponentType.Button)
    }
}

/**
 * PatternFly [link button](https://www.patternfly.org/v4/components/button/design-guidelines#link-button) component based on an `<a/>` element.
 *
 * Links buttons are labeled buttons with no background or border.
 *
 * @sample ButtonSamples.linkButton
 */
public class LinkButton internal constructor(
    variations: Array<out ButtonVariation>,
    id: String?,
    baseClass: String?,
    job: Job
) : A(id, classes {
    +ComponentType.Button
    +variations.joinToString(" ") { it.modifier }
    +baseClass
}, job), ButtonLike, PatternFlyComponent<HTMLAnchorElement> {

    init {
        markAs(ComponentType.Button)
    }
}

/**
 * Container for an icon inside a [PushButton] or [LinkButton]. The container controls a margin between the icon and the button text depending on the value of [IconPosition].
 */
public class ButtonIcon internal constructor(
    iconPosition: IconPosition,
    iconClass: String,
    id: String?,
    baseClass: String?,
    job: Job,
    content: Icon.() -> Unit
) : Span(baseClass = classes {
    +"button".component("icon")
    +iconPosition.modifier
}, job = job) {

    init {
        icon(iconClass, id = id, baseClass = baseClass) {
            content(this)
        }
    }
}
