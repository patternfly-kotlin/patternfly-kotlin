package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.DividerVariant.HR
import org.patternfly.DividerVariant.LI
import org.patternfly.divider
import org.patternfly.layout

internal interface DividerSample {

    fun divider() {
        render {
            p { +"First paragraph" }
            divider(HR)
            p { +"Second paragraph" }

            ul {
                li { +"First item" }
                divider(LI)
                li { +"Second item" }
            }

            div(baseClass = "flex".layout()) {
                div { +"First item" }
                divider(vertical = true)
                div { +"Second item" }
            }
        }
    }
}
