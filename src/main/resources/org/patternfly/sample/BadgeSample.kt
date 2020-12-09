package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.flowOf
import org.patternfly.badge

internal interface BadgeSample {

    fun badge() {
        render {
            val values = flowOf(1, 2, 3)
            badge { +"Label" }
            badge {
                value("Label")
            }
            badge {
                value(23)
            }
            badge {
                value(values)
            }
        }
    }
}
