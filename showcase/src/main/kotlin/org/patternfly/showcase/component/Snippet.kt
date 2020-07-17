package org.patternfly.showcase.component

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.HtmlElements
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import org.patternfly.fas
import org.patternfly.pfPlainButton
import org.patternfly.util
import org.w3c.dom.HTMLElement

fun HtmlElements.snippet(header: String, code: String, content: HtmlElements.() -> Unit): Snippet =
    register(Snippet(header, code, content), {})

internal class CodeStore : RootStore<Boolean>(true) {
    val toggle = handle { hidden -> !hidden }
}

@OptIn(ExperimentalCoroutinesApi::class)
class Snippet(header: String, code: String, val content: HtmlElements.() -> Unit) :
    Tag<HTMLElement>("section", baseClass = "sc-snippet") {
    private val codeStore = CodeStore()

    init {
        h2("sc-snippet__heading") { +header }
        div("sc-snippet__content") {
            this@Snippet.content(this)
        }
        div("sc-snippet__toolbar") {
            pfPlainButton(iconClass = "code".fas()) {
                clicks handledBy this@Snippet.codeStore.toggle
            }
            pfPlainButton(iconClass = "copy".fas()) {
                // TODO copy to clipboard
            }
        }
        div("sc-snippet__code") {
            classMap = this@Snippet.codeStore.data.map { hidden ->
                mapOf("display-none".util() to hidden)
            }
            pre("prettyprint") {
                +code
            }
        }
    }
}
