@file:Suppress("SpellCheckingInspection")

package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.routing.router
import org.patternfly.avatar
import org.patternfly.breadcrumb
import org.patternfly.item
import org.patternfly.items

internal interface BreadcrumbSample {

    fun breadcrumb() {
        render {
            val router = router("home")
            breadcrumb(router) {
                items {
                    item("universe", "Universe")
                    item("milky-way", "Milky way")
                    item("solar-system", "Solar system")
                    item("earth", "Earth")
                    item("europe", "Europe")
                    item("germany", "Germany")
                    item("wuerzburg", "Würzburg")
                }
            }
        }
    }
}
