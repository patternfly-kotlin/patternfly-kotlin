package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Level.H1
import org.patternfly.Level.H2
import org.patternfly.Level.H3
import org.patternfly.Size
import org.patternfly.title

internal class TitleSample {

    fun title() {
        render {
            title(H1) { +"Level 1" }
            title(H2, title = "Level 2")
            title(H3, size = Size.SM) { +"Level 3" }
        }
    }
}
