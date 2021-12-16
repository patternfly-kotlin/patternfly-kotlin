package org.patternfly

import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// ------------------------------------------------------ factory

/**
 * Creates an [Icon] component.
 *
 * @param iconClass the CSS icon class. Use one of the string extension methods [String.far], [String.fas] or [String.pfIcon] to create a valid CSS class.
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.IconSample.icons
 */
public fun RenderContext.icon(
    iconClass: String = "",
    baseClass: String? = null,
    id: String? = null,
    context: Icon.() -> Unit = {}
): TextElement = Icon(iconClass).apply(context).render(this, baseClass, id)

// ------------------------------------------------------ component

/**
 * PatternFly [icon](https://www.patternfly.org/v4/guidelines/icons/) component.
 *
 * PatternFly uses icons from [Font Awesome Free](https://fontawesome.com/icons?d=gallery&m=free). In addition it comes with its own icon set (with prefix `pf-icon`).
 *
 * Use one of the string extension methods [String.far], [String.fas] or [String.pfIcon] to create a valid CSS class.
 *
 * @sample org.patternfly.sample.IconSample.icons
 */
public open class Icon(iconClass: String) :
    PatternFlyComponent<TextElement>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var classes: Flow<String> = flowOf(iconClass)

    /**
     * Changes the icon class.
     */
    public fun iconClass(iconClass: String) {
        iconClass(flowOf(iconClass))
    }

    /**
     * Changes the icon class.
     */
    public fun iconClass(iconClass: Flow<String>) {
        this.classes = iconClass
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): TextElement = with(context) {
        i(
            baseClass = classes {
                +ComponentType.Icon
                +baseClass
            },
            id = id
        ) {
            markAs(ComponentType.Icon)
            applyElement(this)
            applyEvents(this)

            aria["hidden"] = true
            className(classes)
        }
    }
}
