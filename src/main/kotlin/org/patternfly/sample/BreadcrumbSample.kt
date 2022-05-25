@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import org.patternfly.Severity.INFO
import org.patternfly.breadcrumb
import org.patternfly.dom.Id
import org.patternfly.notification

internal class BreadcrumbSample {

    fun staticItems() {
        render {
            breadcrumb {
                item("Universe") {
                    events {
                        clicks handledBy notification(INFO, "You like it big")
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
        val idProvider: IdProvider<String, String> = { Id.build(it) }
        val selection: Store<String?> = storeOf(idProvider("Würzburg"))
        val values: Store<List<String>> = storeOf(
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
                items(values, idProvider, selection) { place ->
                    item(place)
                }
            }
        }
    }
}
