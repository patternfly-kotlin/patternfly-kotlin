package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.FontSize.XL_4
import org.patternfly.Height._66
import org.patternfly.Shape.CIRCLE
import org.patternfly.Width.SM
import org.patternfly.Width._33
import org.patternfly.skeleton
import org.patternfly.skeleton2

internal class SkeletonSample {

    fun skeletons() {
        render {
            skeleton()
            skeleton(fontSize = XL_4)
            skeleton(width = _33, height = _66)
            skeleton(shape = CIRCLE, width = SM)
            skeleton {
                +"Content loading..."
            }
        }
    }

    fun skeletons2() {
        render {
            skeleton2 {}
            skeleton2 {
                fontSize(XL_4)
            }
            skeleton2 {
                width(_33)
                height(_66)
            }
            skeleton2 {
                shape(CIRCLE)
                width(SM)
            }
            skeleton2 {
                text("Content loading...")
            }
        }
    }
}
