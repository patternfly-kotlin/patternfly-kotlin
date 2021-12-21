@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.breadcrumb
import org.patternfly.notification

internal class BreadcrumbSample {

    fun staticItems() {
        render {
            breadcrumb {
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
            }
        }
    }

    fun dynamicItems() {
        val store = storeOf(
            listOf(
                "Milky way",
                "Solar system",
                "Earth",
                "Europe",
                "Germany",
                "Würzburg",
            )
        )
        render {
            breadcrumb(noHomeLink = true) {
                item("Universe") // universe is always there!
                items(store) { place ->
                    item(place)
                }
            }
        }
    }
}
