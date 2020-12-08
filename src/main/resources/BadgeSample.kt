package org.patternfly

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.flowOf

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
