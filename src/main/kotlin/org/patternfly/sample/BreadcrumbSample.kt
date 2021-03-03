@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Notification
import org.patternfly.breadcrumb
import org.patternfly.item
import org.patternfly.items
import org.patternfly.unwrap

internal interface BreadcrumbSample {

    fun breadcrumb() {
        render {
            breadcrumb<String> {
                store.singleSelection handledBy Notification.add {
                    info("You are here: ${it.unwrap()}")
                }
                items {
                    item("Universe")
                    item("Milky way")
                    item("Solar system")
                    item("Earth")
                    item("Europe")
                    item("Germany")
                    item("Würzburg")
                }
            }
        }
    }
}
