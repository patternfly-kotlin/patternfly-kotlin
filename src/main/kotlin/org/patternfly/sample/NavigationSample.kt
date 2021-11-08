@file:Suppress("DuplicatedCode")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.router
import org.patternfly.NavigationGroup
import org.patternfly.NavigationItem
import org.patternfly.NavigationStore
import org.patternfly.Severity.INFO
import org.patternfly.navigation
import org.patternfly.notification
import org.patternfly.page
import org.patternfly.pageSubNav

internal class NavigationSample {

    fun horizontal() {
        render {
            page {
                masthead {
                    navigation(router("home")) {
                        item("get-started", "Get Started")
                        item("get-in-touch", "Get in Touch")
                    }
                }
            }
        }
    }

    fun horizontalSubNav() {
        render {
            page {
                main {
                    pageSubNav {
                        navigation(router("home")) {
                            item("get-started", "Get Started")
                            item("get-in-touch", "Get in Touch")
                        }
                    }
                }
            }
        }
    }

    fun vertical() {
        render {
            page {
                sidebar {
                    navigation(router("home"), expandable = true) {
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
        render {
            navigation(
                router("home"),
                NavigationStore(
                    listOf(
                        NavigationItem("get-started", "Get Started"),
                        NavigationItem("get-in-touch", "Get in Touch"),
                        NavigationGroup(
                            "Components", listOf(
                                NavigationItem("component", "Some component")
                            )
                        )
                    )
                )
            ) {
                store.clicks handledBy notification(INFO) { item ->
                    title("Clicked on ${item.title}, going to ${item.route}")
                }
            }
        }
    }
}
