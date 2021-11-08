@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.breadcrumb
import org.patternfly.item
import org.patternfly.items
import org.patternfly.notification
import org.patternfly.unwrap

internal interface BreadcrumbSample {

    fun breadcrumb() {
        render {
            breadcrumb<String> {
                store.singleSelection handledBy notification(INFO) { item ->
                    title("You are here: ${item.unwrap()}")
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
