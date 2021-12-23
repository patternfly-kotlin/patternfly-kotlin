package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.emptyState
import org.patternfly.fas

internal class EmptyStateSample {

    fun basicSetup() {
        render {
            emptyState(iconClass = "cubes".fas(), title = "Empty State") {
                content {
                    +"This represents an the empty state pattern in PatternFly 4."
                }
                primaryAction { +"Primary action" }
                secondaryAction { +"Multiple" }
                secondaryAction { +"Action buttons" }
                secondaryAction { +"Can" }
                secondaryAction { +"Go here" }
                secondaryAction { +"In the" }
                secondaryAction { +"Secondary" }
                secondaryAction { +"Area" }
            }
        }
    }
}
