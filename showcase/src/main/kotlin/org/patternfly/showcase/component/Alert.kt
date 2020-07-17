package org.patternfly.showcase.component

import dev.fritz2.binding.const
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
                p {
                    strong { +"Alerts" }
                    +" are used to notify the user about a change in status or other event. Related design guidelines: "
                    a {
                        href =
                            const("https://www.patternfly.org/v4/design-guidelines/usage-and-behavior/alerts-and-notifications")
                        target = const("pf4")
                        +"Alerts and notifications"
                    }
                }
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
