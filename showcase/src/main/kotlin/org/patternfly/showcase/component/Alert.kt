package org.patternfly.showcase.component

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.render
import org.patternfly.Severity
import org.patternfly.pfAlert
import org.patternfly.pfSection
import org.w3c.dom.HTMLElement

object AlertComponent : Iterable<Tag<HTMLElement>> {
    override fun iterator(): Iterator<Tag<HTMLElement>> = iterator {
        yield(render {
            component("Alert") {
                p { +"Lorem ipsum, ..." }
            }
        })
        yield(render {
            pfSection {
                snippet("Types", AlertCode.types) {
                    pfAlert(Severity.INFO, "Info Alert")
                    pfAlert(Severity.WARNING, "Warning Alert")
                }
            }
        })
    }
}

internal object AlertCode {
    const val types: String = """
import dev.fritz2.dom.html.render
import org.patternfly.Severity
import org.patternfly.pfAlert

fun main() {
    render {
        pfAlert(Severity.INFO, "Info Alert")
        pfAlert(Severity.WARNING, "Warning Alert")
    }
}
"""
}
