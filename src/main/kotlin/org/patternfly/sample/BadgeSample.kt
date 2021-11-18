package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.flowOf
import org.patternfly.badge

internal class BadgeSample {

    fun badge() {
        render {
            badge(23)

            val values = flowOf(10, 200, 3000)
            badge(max = 200) {
                count(values)
            }
        }
    }
}
