package org.patternfly

import dev.fritz2.dom.html.Img
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLImageElement

// ------------------------------------------------------ dsl

/**
 * Creates an [Avatar] component.
 *
 * @param src specifies the URL of the image for the avatar
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.avatar(
    src: String,
    id: String? = null,
    baseClass: String? = null,
    content: Avatar.() -> Unit = {}
): Avatar = register(Avatar(src, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [avatar](https://www.patternfly.org/v4/components/avatar/design-guidelines) component.
 *
 * An avatar is a visual used to represent a user. It may contain an image or a placeholder graphic. Typical usage is to represent the current user in the masthead.
 *
 * @sample org.patternfly.sample.AvatarSample.avatar
 */
public class Avatar internal constructor(src: String, id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLImageElement>,
    Img(
        id = id,
        baseClass = classes {
            +ComponentType.Avatar
            +baseClass
        },
        job = job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Avatar)
        src(src)
    }
}
