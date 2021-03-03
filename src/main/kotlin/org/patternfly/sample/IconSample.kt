package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.far
import org.patternfly.fas
import org.patternfly.icon
import org.patternfly.pfIcon

internal interface IconSample {

    fun icons() {
        render {
            icon("bundle".pfIcon())
            icon("clock".far())
            icon("bars".fas())
        }
    }
}
