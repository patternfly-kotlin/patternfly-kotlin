@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import dev.fritz2.routing.router
import org.patternfly.Severity.INFO
import org.patternfly.breadcrumb
import org.patternfly.notification

internal class BreadcrumbSample {

    fun staticItems() {
        render {
            breadcrumb<String> {
                item("Universe") {
                    events {
                        clicks handledBy notification(INFO, "The very beginning")
                    }
                }
                item("Milky way")
                item("Solar system")
                item("Earth")
                item("Europe")
                item("Germany")
                item("Würzburg")
                events {
                    selections handledBy notification(INFO) { item ->
                        title("You are here: $item")
                    }
                }
            }
        }
    }

    fun storeItems() {
        render {
            val store = storeOf(
                listOf(
                    "Universe",
                    "Milky way",
                    "Solar system",
                    "Earth",
                    "Europe",
                    "Germany",
                    "Würzburg",
                )
            )

            breadcrumb(store = store) {
                display {
                    item(it)
                }
                events {
                    selections handledBy notification(INFO) { item ->
                        title("You are here: $item")
                    }
                }
            }
        }
    }

    fun routerItems() {
        render {
            val router = router("home")

            breadcrumb(router = router) {
                item("user-management") { +"User Managewment" }
                item("users") { +"Users" }
                item("current-user") { +"Current User" }
            }
        }
    }
}
