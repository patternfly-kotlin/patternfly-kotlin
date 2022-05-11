package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Shape.CIRCLE
import org.patternfly.TextSize.XL_4
import org.patternfly.skeleton

internal class SkeletonSample {

    fun skeletons() {
        render {
            skeleton()
            skeleton(textSize = XL_4)
            skeleton(width = "33%", height = "66%")
            skeleton(shape = CIRCLE, width = "5rem")
            skeleton {
                +"Content loading..."
            }
        }
    }
}
