package org.patternfly

import dev.fritz2.binding.each
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import org.patternfly.SelectionMode.NONE
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfCardView(
    store: ItemStore<T>,
    selectionMode: SelectionMode = NONE,
    classes: String? = null, content: CardView<T>.() -> Unit = {}
): CardView<T> = register(CardView(store, selectionMode, classes), content)

// ------------------------------------------------------ tag

class CardView<T> internal constructor(
    val store: ItemStore<T>,
    private val selectionMode: SelectionMode,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
        +"gallery".layout()
        "gutter".modifier()
        +classes
    }) {

    var asText: AsText<T> = { it.toString() }
    var display: ComponentDisplay<Card, T> = {
        {
            pfCardBody {
                +this@CardView.asText.invoke(it)
            }
        }
    }

    init {
        markAs(ComponentType.CardView)
        store.visible.each { store.identifier(it) }.render { item ->
            pfCard(selectable = this@CardView.selectionMode != NONE) {
                val content = this@CardView.display.invoke(item)
                content.invoke(this)
            }
        }.bind()
    }
}
