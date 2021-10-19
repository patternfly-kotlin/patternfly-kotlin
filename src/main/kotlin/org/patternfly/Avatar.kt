package org.patternfly

import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.RenderContext
import org.w3c.dom.HTMLImageElement

// ------------------------------------------------------ factory

/**
 * Creates an [Avatar] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param build a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AvatarSample.avatar
 */
public fun RenderContext.avatar(
    baseClass: String? = null,
    id: String? = null,
    build: Avatar.() -> Unit
) {
    Avatar().apply(build).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [avatar](https://www.patternfly.org/v4/components/avatar/design-guidelines) component.
 *
 * An avatar is a visual used to represent a user. It may contain an image or a placeholder graphic. Typical usage is to represent the current user in the masthead.
 *
 * @sample org.patternfly.sample.AvatarSample.avatar
 */
public class Avatar :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement<Img, HTMLImageElement> by ElementMixin(),
    WithEvents<HTMLImageElement> by EventMixin() {

    private var src: String = ""

    public fun src(src: String) {
        this.src = src
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            img(
                baseClass = classes {
                    +ComponentType.Avatar
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Avatar)
                ariaContext.applyTo(this)
                element(this)
                events(this)

                src(this@Avatar.src)
            }
        }
    }
}
