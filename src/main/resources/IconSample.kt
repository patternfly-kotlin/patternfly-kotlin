package org.patternfly

import dev.fritz2.dom.html.render

internal interface IconSample {

    fun icons() {
        render {
            icon("bundle".pfIcon())
            icon("clock".far())
            icon("bars".fas())
        }
    }
}
