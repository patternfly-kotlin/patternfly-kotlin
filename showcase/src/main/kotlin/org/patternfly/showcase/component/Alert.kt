@file:Suppress("DuplicatedCode")

package org.patternfly.showcase.component

import dev.fritz2.binding.const
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.render
import org.patternfly.Modifier.inline
import org.patternfly.Modifier.link
import org.patternfly.Severity.DANGER
import org.patternfly.Severity.DEFAULT
import org.patternfly.Severity.INFO
import org.patternfly.Severity.SUCCESS
import org.patternfly.Severity.WARNING
import org.patternfly.Size
import org.patternfly.pfAlert
import org.patternfly.pfAlertActionGroup
import org.patternfly.pfAlertDescription
import org.patternfly.pfButton
import org.patternfly.pfContent
import org.patternfly.pfSection
import org.patternfly.pfTitle
import org.patternfly.showcase.Places.behaviour
import org.patternfly.util
import org.w3c.dom.HTMLElement

object AlertComponent : Iterable<Tag<HTMLElement>> {
    override fun iterator(): Iterator<Tag<HTMLElement>> = iterator {
        yield(render {
            pfSection("pb-0".util()) {
                pfContent {
                    pfTitle("Alert", size = Size.XL_3)
                    p {
                        strong { +"Alerts" }
                        +" are used to notify the user about a change in status or other event. Related design guidelines: "
                        a {
                            href = const(behaviour("alerts-and-notifications"))
                            target = const("pf4")
                            +"Alerts and notifications"
                        }
                    }
                }
            }
        })
        yield(render {
            pfSection {
                pfContent {
                    h2 { +"Examples" }
                }
                snippet("Types", AlertCode.TYPES) {
                    pfAlert(DEFAULT, "Default alert title")
                    br {}
                    pfAlert(INFO, "Info alert title")
                    br {}
                    pfAlert(SUCCESS, "Success alert title")
                    br {}
                    pfAlert(WARNING, "Warning alert title")
                    br {}
                    pfAlert(DANGER, "Danger alert title")
                }
                snippet("Variations", AlertCode.VARIATIONS) {
                    pfAlert(SUCCESS, "Success alert title", closable = true) {
                        pfAlertDescription {
                            p { +"Success alert description. This should tell the user more information about the alert." }
                        }
                        pfAlertActionGroup {
                            pfButton(link, inline) { +"View details" }
                            pfButton(link, inline) { +"Ignore" }
                        }
                    }
                    br {}
                    pfAlert(SUCCESS, "Success alert title", closable = true) {
                        pfAlertDescription {
                            p {
                                +"Success alert description. This should tell the user more information about the alert. "
                                a {
                                    href = const("#")
                                    +"This is a link."
                                }
                            }
                        }
                    }
                    br {}
                    pfAlert(SUCCESS, "Success alert title", closable = true) {
                        pfAlertActionGroup {
                            pfButton(link, inline) { +"View details" }
                            pfButton(link, inline) { +"Ignore" }
                        }
                    }
                    br {}
                    pfAlert(SUCCESS, "Success alert title", closable = true)
                    br {}
                    pfAlert(SUCCESS, "Success alert title")
                }
                snippet("Inline types", AlertCode.INLINE_TYPES) {
                    pfAlert(DEFAULT, "Default inline alert title", inline = true)
                    br {}
                    pfAlert(INFO, "Info inline alert title", inline = true)
                    br {}
                    pfAlert(SUCCESS, "Success inline alert title", inline = true)
                    br {}
                    pfAlert(WARNING, "Warning inline alert title", inline = true)
                    br {}
                    pfAlert(DANGER, "Danger inline alert title", inline = true)
                }
                snippet("Inline Variations", AlertCode.INLINE_VARIATIONS) {
                    pfAlert(SUCCESS, "Success alert title", closable = true, inline = true) {
                        pfAlertDescription {
                            p { +"Success alert description. This should tell the user more information about the alert." }
                        }
                        pfAlertActionGroup {
                            pfButton(link, inline) { +"View details" }
                            pfButton(link, inline) { +"Ignore" }
                        }
                    }
                    br {}
                    pfAlert(SUCCESS, "Success alert title", closable = true, inline = true) {
                        pfAlertDescription {
                            p {
                                +"Success alert description. This should tell the user more information about the alert. "
                                a {
                                    href = const("#")
                                    +"This is a link."
                                }
                            }
                        }
                    }
                    br {}
                    pfAlert(SUCCESS, "Success alert title", closable = true, inline = true) {
                        pfAlertActionGroup {
                            pfButton(link, inline) { +"View details" }
                            pfButton(link, inline) { +"Ignore" }
                        }
                    }
                    br {}
                    pfAlert(SUCCESS, "Success alert title", closable = true, inline = true)
                    br {}
                    pfAlert(SUCCESS, "Success alert title", inline = true)
                }
            }
        })
    }
}

internal object AlertCode {

    //language=kotlin
    const val TYPES: String = """fun main() {
    render {
        pfAlert(DEFAULT, "Default alert title")
        pfAlert(INFO, "Info alert title")
        pfAlert(SUCCESS, "Success alert title")
        pfAlert(WARNING, "Warning alert title")
        pfAlert(DANGER, "Danger alert title")
    }
}
"""

    //language=kotlin
    const val VARIATIONS: String = """fun main() {
    render {
        pfAlert(SUCCESS, "Success alert title", closable = true) {
            pfAlertDescription {
                p { +"Success alert description. This should tell the user more information about the alert." }
            }
            pfAlertActionGroup {
                pfButton(link, inline) { +"View details" }
                pfButton(link, inline) { +"Ignore" }
            }
        }
        pfAlert(SUCCESS, "Success alert title", closable = true) {
            pfAlertDescription {
                p {
                    +"Success alert description. This should tell the user more information about the alert. "
                    a {
                        href = const("#")
                        +"This is a link."
                    }
                }
            }
        } 
        pfAlert(SUCCESS, "Success alert title", closable = true) {
            pfAlertActionGroup {
                pfButton(link, inline) { +"View details" }
                pfButton(link, inline) { +"Ignore" }
            }
        }
        pfAlert(SUCCESS, "Success alert title", closable = true)
        pfAlert(SUCCESS, "Success alert title")
    }
}
"""

    //language=kotlin
    const val INLINE_TYPES: String = """fun main() {
    render {
        pfAlert(DEFAULT, "Default inline alert title", inline = true)
        pfAlert(INFO, "Info inline alert title", inline = true)
        pfAlert(SUCCESS, "Success inline alert title", inline = true)
        pfAlert(WARNING, "Warning inline alert title", inline = true)
        pfAlert(DANGER, "Danger inline alert title", inline = true)
    }
}
"""

    //language=kotlin
    const val INLINE_VARIATIONS: String = """fun main() {
    render {
        pfAlert(SUCCESS, "Success alert title", closable = true, inline = true) {
            pfAlertDescription {
                p { +"Success alert description. This should tell the user more information about the alert." }
            }
            pfAlertActionGroup {
                pfButton(link, inline) { +"View details" }
                pfButton(link, inline) { +"Ignore" }
            }
        }
        pfAlert(SUCCESS, "Success alert title", closable = true, inline = true) {
            pfAlertDescription {
                p {
                    +"Success alert description. This should tell the user more information about the alert. "
                    a {
                        href = const("#")
                        +"This is a link."
                    }
                }
            }
        } 
        pfAlert(SUCCESS, "Success alert title", closable = true, inline = true) {
            pfAlertActionGroup {
                pfButton(link, inline) { +"View details" }
                pfButton(link, inline) { +"Ignore" }
            }
        }
        pfAlert(SUCCESS, "Success alert title", closable = true, inline = true)
        pfAlert(SUCCESS, "Success alert title", inline = true)
    }
}
"""
}
