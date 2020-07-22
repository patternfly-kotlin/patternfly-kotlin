package org.patternfly.showcase

import dev.fritz2.binding.const
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.render
import org.patternfly.Modifier.light
import org.patternfly.pfContent
import org.patternfly.pfSection
import org.patternfly.pfTitle
import org.w3c.dom.HTMLElement

object HomePage : Iterable<Tag<HTMLElement>> {
    override fun iterator(): Iterator<Tag<HTMLElement>> = iterator {
        yield(render {
            pfSection(light) {
                pfContent {
                    pfTitle("PatternFly Fritz2")
                    p {
                        +"PatternFly Fritz2 is a "
                        a {
                            +"Kotlin / JS"
                            href = const("https://kotlinlang.org/docs/reference/js-overview.html")
                        }
                        +" implementation of "
                        a {
                            +"PatternFly"
                            href = const("https://www.patternfly.org/")
                        }
                        +" based on "
                        a {
                            +"fritz2"
                            href = const("https://docs.fritz2.dev/")
                        }
                        +"."
                    }
                }
            }
        })
    }
}
