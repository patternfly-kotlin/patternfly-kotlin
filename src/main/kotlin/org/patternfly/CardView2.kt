package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.render
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.patternfly.CardVariant.compact
import org.patternfly.dom.Id

// ------------------------------------------------------ factory

/**
 * Creates an [CardView2] component.
 *
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.cardView2(
    baseClass: String? = null,
    id: String? = null,
    context: CardView2.() -> Unit = {}
): CardView2 = CardView2().apply(context).render(this, baseClass, id)

internal fun testCardView() {
    render {
        cardView2 {
            card(compact) {

            }
        }
    }
}

// ------------------------------------------------------ component

public open class CardView2 :
    PatternFlyComponent<CardView2>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var itemsInStore: Boolean = false
    private val itemStore: CardViewItemStore = CardViewItemStore()
    private val headItems: MutableList<CardViewItem> = mutableListOf()
    private val tailItems: MutableList<CardViewItem> = mutableListOf()
    private val singleIdSelection: RootStore<String?> = storeOf(null)
    private val multiIdSelection: MultiIdSelectionStore = MultiIdSelectionStore()

    public val selectedId: Flow<String?>
        get() = singleIdSelection.data

    public val selectedIds: Flow<List<String>>
        get() = multiIdSelection.data

    public fun card(
        vararg variants: CardVariant,
        toggleRight: Boolean = false,
        baseClass: String? = null,
        id: String = Id.unique(ComponentType.CardView.id, "itm"),
        context: Card.() -> Unit
    ) {
        (if (itemsInStore) tailItems else headItems).add(CardViewItem(variants, toggleRight, baseClass, id, context))
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): CardView2 = with(context) {
        ul(
            baseClass = classes {
                +"gallery".layout()
                +"gutter".modifier()
                +baseClass
            },
            id = id
        ) {
            markAs(ComponentType.CardView)
            applyElement(this)
            applyEvents(this)

            itemStore.data.map { items ->
                headItems + items + tailItems
            }.renderEach(into = this, idProvider = { it.id }) { item ->
                Card(item.variants, item.toggleRight).apply { item.context }.render(
                    this, item.baseClass, item.id
                )
            }
        }
        this@CardView2
    }

}


// ------------------------------------------------------ item & store

internal class CardViewItem(
    val variants: Array<out CardVariant>,
    val toggleRight: Boolean,
    val baseClass: String?,
    val id: String,
    val context: Card.() -> Unit
)

internal class CardViewItemStore : RootStore<List<CardViewItem>>(emptyList())
