@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.router
import org.patternfly.group
import org.patternfly.groups
import org.patternfly.horizontalNavigation
import org.patternfly.item
import org.patternfly.items
import org.patternfly.tertiaryNavigation
import org.patternfly.verticalNavigation

internal interface NavigationSample {

    fun horizontal() {
        render {
            val router = router("home")
            horizontalNavigation(router) {
                items {
                    item("get-started", "Get Started")
                    item("get-in-touch", "Get in Touch")
                }
            }
        }
    }

    fun tertiary() {
        render {
            val router = router("home")
            tertiaryNavigation(router) {
                items {
                    item("get-started", "Get Started")
                    item("get-in-touch", "Get in Touch")
                }
            }
        }
    }

    fun vertical() {
        render {
            val router = router("home")
            verticalNavigation(router) {
                items {
                    item("get-started", "Get Started")
                    item("get-in-touch", "Get in Touch")
                }
            }
        }
    }

    fun expandable() {
        render {
            val router = router("home")
            verticalNavigation(router, expandable = true) {
                groups {
                    group {
                        item("get-started", "Get Started")
                        item("get-in-touch", "Get in Touch")
                    }
                    group("Components") {
                        item("component", "Some component")
                    }
                    group("Demos") {
                        item("demo", "Some demo")
                    }
                }
            }
        }
    }
}
