package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Level.H1
import org.patternfly.Level.H2
import org.patternfly.Level.H3
import org.patternfly.Level.H4
import org.patternfly.Level.H5
import org.patternfly.Level.H6
import org.patternfly.title

internal interface TitleSample {

    fun title() {
        render {
            title(H1) { +"Level 1" }
            title(H2) { +"Level 2" }
            title(H3) { +"Level 3" }
            title(H4) { +"Level 4" }
            title(H5) { +"Level 5" }
            title(H6) { +"Level 6" }
        }
    }
}
