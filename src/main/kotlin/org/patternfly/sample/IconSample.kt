package org.patternfly.sample

import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.flowOf
import org.patternfly.far
import org.patternfly.fas
import org.patternfly.icon
import org.patternfly.pfIcon

internal class IconSample {

    fun icons() {
        render {
            icon("bundle".pfIcon())
            icon("clock".far())
            icon("bars".fas())

            val icons = flowOf(
                "bundle".pfIcon(),
                "clock".far(),
                "bars".fas()
            )
            icon {
                iconClass(icons)
            }
        }
    }
}
