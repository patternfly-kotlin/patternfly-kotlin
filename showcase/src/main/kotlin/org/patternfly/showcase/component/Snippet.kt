package org.patternfly.showcase.component

import ClipboardJS
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.HtmlElements
import hljs.highlightBlock
import kotlinx.browser.window
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import org.patternfly.By
import org.patternfly.ComponentType
import org.patternfly.Dataset
import org.patternfly.Id
import org.patternfly.Modifier.plain
import org.patternfly.fas
import org.patternfly.minusAssign
import org.patternfly.pfButton
import org.patternfly.pfContent
import org.patternfly.pfIcon
import org.patternfly.plusAssign
import org.patternfly.querySelector
import org.patternfly.util
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement

fun HtmlElements.snippet(header: String, code: String, content: HtmlElements.() -> Unit): Snippet =
    register(Snippet(header, code, content), {})

internal class CodeStore : RootStore<Boolean>(true) {
    val toggle = handle { hidden -> !hidden }
}

@OptIn(ExperimentalCoroutinesApi::class)
class Snippet(header: String, code: String, val content: HtmlElements.() -> Unit) :
    Tag<HTMLElement>("section", baseClass = "sc-snippet") {
    private val copiedId = Id.unique()
    private var timeoutHandle = -1
    private val codeStore = CodeStore()

    init {
        h3("sc-snippet__heading") { +header }
        div("sc-snippet__content") {
            this@Snippet.content(this)
        }
        div("sc-snippet__toolbar") {
            pfButton(plain) {
                pfIcon("code".fas())
                clicks handledBy this@Snippet.codeStore.toggle
            }
            pfButton(plain) {
                pfIcon("copy".fas())
                val clipboard = ClipboardJS(domNode, object : ClipboardJS.Options {
                    override val text: ((Element) -> String)? = { code }
                })
                clipboard.on("success") {
                    this@Snippet.showCopied()
                }
            }
            pfContent {
                domNode.classList += "display-none".util()
                span("sc-snippet__copied") {
                    +"Copied to clipboard"
                }
            }
        }
        div("sc-snippet__code") {
            classMap = this@Snippet.codeStore.data.map { hidden ->
                mapOf("display-none".util() to hidden)
            }
            pre {
                code("kotlin") {
                    +code
                    highlightBlock(domNode)
                }
            }
        }
    }

    private fun showCopied() {
        window.clearTimeout(timeoutHandle)
        domNode.querySelector(By.data(Dataset.COMPONENT_TYPE.short, ComponentType.Content.id))?.let {
            it.classList -= "display-none".util()
            timeoutHandle = window.setTimeout({
                it.classList += "display-none".util()
            }, 2000)
        }
    }
}
