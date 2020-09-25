package org.patternfly

import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfCardView(
    store: ItemStore<T>,
    classes: String? = null, content: CardView<T>.() -> Unit = {}
): CardView<T> = register(CardView(store, classes), content)

fun <T> CardCheckbox.bind(store: ItemStore<T>, item: T) {
    changes.states().map { Pair(item, it) } handledBy store.select
    checked = store.data.map { it.isSelected(item) }
}

// ------------------------------------------------------ tag

class CardView<T> internal constructor(
    val store: ItemStore<T>,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>,
    Div(baseClass = classes {
        +"gallery".layout()
        +"gutter".modifier()
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
            pfCard {
                val content = this@CardView.display.invoke(item)
                content.invoke(this)
            }
        }.bind()
    }
}
