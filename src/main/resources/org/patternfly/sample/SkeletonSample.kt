package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.FontSize
import org.patternfly.Height
import org.patternfly.Shape
import org.patternfly.Width
import org.patternfly.skeleton

internal interface SkeletonSample {
    fun skeletons() {
        render {
            skeleton()
            skeleton(id = "skeleton-with-custom-font-size", fontSize = FontSize.XL_4)
            skeleton(id = "skeleton-with-custom-width-and-height", width = Width._33, height = Height._66)
            skeleton(id = "skeleton-as-a-circle", shape = Shape.CIRCLE, width = Width.SM)
            skeleton(id = "skeleton-with-accessible-text") { +"Content loading..." }
        }
    }
}
