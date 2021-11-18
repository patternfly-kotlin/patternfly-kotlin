package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Size.MD
import org.patternfly.spinner

internal class SpinnerSample {

    fun spinner() {
        render {
            spinner(size = MD)
        }
    }
}
