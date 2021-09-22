package org.patternfly

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.patternfly.dom.aria
import org.w3c.dom.HTMLElement

// ------------------------------------------------------ dsl

/**
 * Creates an [Icon] component.
 *
 * @param iconClass the CSS icon class. Use one of the string extension methods [String.far], [String.fas] or [String.pfIcon] to create a valid CSS class.
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.icon(
    iconClass: String,
    id: String? = null,
    baseClass: String? = null,
    content: Icon.() -> Unit = {}
): Icon = register(Icon(iconClass, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [icon](https://www.patternfly.org/v4/guidelines/icons/) component.
 *
 * PatternFly uses icons from [Font Awesome Free](https://fontawesome.com/icons?d=gallery&m=free). In addition it comes with its own icon set (with prefix `pf-icon`).
 *
 * Use one of the string extension methods [String.far], [String.fas] or [String.pfIcon] to create a valid CSS class.
 *
 * @sample org.patternfly.sample.IconSample.icons
 */
public class Icon internal constructor(iconClass: String, id: String?, baseClass: String?, job: Job) :
    PatternFlyElement<HTMLElement>,
    TextElement(
        "i",
        id = id,
        baseClass = classes {
            +ComponentType.Icon
            +iconClass
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Icon)
        aria["hidden"] = true
    }

    /**
     * Changes the icon class.
     */
    public fun iconClass(iconClass: Flow<String>) {
        mountSingle(job, iconClass) { v, _ ->
            iconClass(v)
        }
    }

    /**
     * Changes the icon class.
     */
    public fun iconClass(iconClass: String) {
        attr(
            "class",
            classes {
                +ComponentType.Icon
                +iconClass
                +baseClass
            }
        )
    }
}
