@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.router
import org.patternfly.Severity.INFO
import org.patternfly.navigation
import org.patternfly.navigationStore
import org.patternfly.notification
import org.patternfly.page
import org.patternfly.pageSubNav

internal class NavigationSample {

    fun horizontal() {
        val router = router("home")
        render {
            page {
                masthead {
                    navigation(router) {
                        item("get-started", "Get Started")
                        item("get-in-touch", "Get in Touch") {
                            events {
                                clicks handledBy notification(INFO, "Custom navigation!")
                            }
                        }
                    }
                }
            }
        }
    }

    fun horizontalSubNav() {
        val router = router("home")
        render {
            page {
                main {
                    pageSubNav {
                        navigation(router) {
                            item("get-started", "Get Started")
                            item("get-in-touch", "Get in Touch")
                        }
                    }
                }
            }
        }
    }

    fun vertical() {
        val router = router("home")
        render {
            page {
                sidebar {
                    navigation(router, expandable = true) {
                        item("get-started", "Get Started")
                        item("get-in-touch", "Get in Touch")
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

    fun store() {
        val router = router("home")
        val store = navigationStore<String> {
            item("get-started", "Get Started")
            item("get-in-touch", "Get in Touch")
            group("Components") {
                item("component", "Some component")
            }
            group("Demos") {
                item("demo", "Some demo")
            }
        }
        render {
            page {
                sidebar {
                    navigation(router, store)
                }
            }
        }
    }
}
