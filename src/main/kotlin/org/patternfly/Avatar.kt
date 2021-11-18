package org.patternfly

import dev.fritz2.dom.html.RenderContext

// ------------------------------------------------------ factory

/**
 * Creates an [Avatar] component.
 *
 * @param src the source of the avatar image
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.AvatarSample.avatar
 */
public fun RenderContext.avatar(
    src: String = "",
    baseClass: String? = null,
    id: String? = null,
    context: Avatar.() -> Unit = {}
) {
    Avatar(src).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [avatar](https://www.patternfly.org/v4/components/avatar/design-guidelines) component.
 *
 * An avatar is a visual used to represent a user. It may contain an image or a placeholder graphic. Typical usage is to represent the current user in the masthead.
 *
 * @sample org.patternfly.sample.AvatarSample.avatar
 */
public class Avatar internal constructor(private var src: String) :
    PatternFlyComponent<Unit>,
    WithAria by AriaMixin(),
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

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
                aria(this)
                element(this)
                events(this)

                src(src)
            }
        }
    }
}
