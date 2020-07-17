package org.patternfly.showcase

import dev.fritz2.routing.Router
import dev.fritz2.routing.StringRoute
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.dom.clear

fun main() {
    kotlinext.js.require("@patternfly/patternfly/patternfly.css")
    kotlinext.js.require("@patternfly/patternfly/patternfly-addons.css")

    val router = Router(StringRoute(Places.HOME))
    document.body?.let { body ->
        Skeleton(router).forEach { body.append(it.domNode) }
    }

    MainScope().launch {
        router.routes.collect { place ->
            document.querySelector("#main")?.let { main ->
                main.clear()
                Places.lookup(place).forEach { main.append(it.domNode) }
            }
        }
    }
}
