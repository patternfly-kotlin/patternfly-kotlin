package org.patternfly.showcase

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.render
import org.patternfly.modifier
import org.patternfly.pfContent
import org.patternfly.pfSection
import org.patternfly.pfTitle
import org.patternfly.showcase.component.AlertComponent
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalStdlibApi::class)
object Places {

    const val CONTRIBUTE = "contribute"
    const val DOCUMENTATION = "documentation"
    const val GET_IN_TOUCH = "get-in-touch"
    const val GET_STARTED = "get-started"
    const val HOME = "home"

    private val tags: Map<String, Iterable<Tag<HTMLElement>>> = buildMap {
        put("home", HomePage)
        put(component("alert"), AlertComponent)
    }

    fun component(id: String): String = "$DOCUMENTATION:component=$id"

    fun demo(id: String): String = "$DOCUMENTATION:demo=$id"

    fun lookup(place: String): Iterable<Tag<HTMLElement>> = tags.getOrElse(place) { notFound(place) }

    private fun notFound(place: String) = listOf(
        render {
            pfSection("light".modifier()) {
                pfContent {
                    pfTitle("Not Found")
                    p {
                        +"Page "
                        code { +place }
                        +" not found"
                    }
                }
            }
        })
}
