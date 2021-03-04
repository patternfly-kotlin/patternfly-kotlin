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
            skeleton(fontSize = FontSize.XL_4)
            skeleton(width = Width._33, height = Height._66)
            skeleton(shape = Shape.CIRCLE, width = Width.SM)
            skeleton {
                +"Content loading..."
            }
        }
    }
}
