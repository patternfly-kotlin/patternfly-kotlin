package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.avatar

internal interface AvatarSample {

    fun avatar() {
        render {
            avatar {
                src("/assets/images/img_avatar.svg")
            }
        }
    }
}
