package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.ButtonVariation.link
import org.patternfly.ButtonVariation.primary
import org.patternfly.emptyState
import org.patternfly.emptyStateBody
import org.patternfly.emptyStatePrimary
import org.patternfly.emptyStateSecondary
import org.patternfly.fas
import org.patternfly.pushButton

internal interface EmptyStateSample {

    fun emptyState() {
        render {
            emptyState(iconClass = "cubes".fas(), title = "Empty State") {
                emptyStateBody {
                    +"This represents an the empty state pattern in PatternFly 4."
                }
                pushButton(primary) {
                    +"Primary action"
                }
                emptyStateSecondary {
                    pushButton(link) { +"Multiple" }
                    pushButton(link) { +"Action buttons" }
                    pushButton(link) { +"Can" }
                    pushButton(link) { +"Go here" }
                    pushButton(link) { +"In the" }
                    pushButton(link) { +"Secondary" }
                    pushButton(link) { +"Area" }
                }
            }
        }
    }

    fun primaryContainer() {
        render {
            emptyState(iconClass = "cubes".fas(), title = "Empty State") {
                emptyStateBody {
                    +"This represents an the empty state pattern in PatternFly 4."
                }
                emptyStatePrimary {
                    pushButton(link) { +"Primary action" }
                }
            }
        }
    }
}
