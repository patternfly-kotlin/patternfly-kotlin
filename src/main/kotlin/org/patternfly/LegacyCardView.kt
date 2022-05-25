package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

/**
 * Creates a [LegacyCardView] component.
 *
 * @param store the item store
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.CardViewSample.cardView
 */
public fun <T> RenderContext.legacyCardView(
    store: ItemsStore<T> = ItemsStore(),
    singleSelection: Boolean = false,
    id: String? = null,
    baseClass: String? = null,
    content: LegacyCardView<T>.() -> Unit = {}
): LegacyCardView<T> = register(LegacyCardView(store, singleSelection, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [card view](https://www.patternfly.org/v4/components/card/design-guidelines/#card-view-usage) component.
 *
 * A card view is a grid of cards that displays a small to moderate amount of content. The card view uses a [display] function to render the items in the [ItemsStore] as [LegacyCard]s.
 *
 * One of the tags used in the [display] function should assign an [element ID][org.w3c.dom.Element.id] based on [ItemsStore.idProvider]. This ID is referenced by various [ARIA labelledby](https://developer.mozilla.org/en-US/docs/Web/Accessibility/ARIA/ARIA_Techniques/Using_the_aria-labelledby_attribute) attributes. Since most of the card components implement [WithIdProvider], this can be easily done using [WithIdProvider.itemId].
 *
 * @param T the type which is used for the [LegacyCard]s in this card view.
 *
 * @sample org.patternfly.sample.CardViewSample.cardView
 */
public class LegacyCardView<T> internal constructor(
    internal val itemsStore: ItemsStore<T>,
    internal val singleSelection: Boolean,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyElement<HTMLDivElement>,
    Div(
        id = id,
        baseClass = classes {
            +"gallery".layout()
            +"gutter".modifier()
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.CardView)
    }

    /**
     * Defines how to display the items in the [ItemsStore] as [LegacyCard]s.
     */
    public fun display(display: (T) -> LegacyCard<T>) {
        itemsStore.page.renderEach({ itemsStore.idProvider(it) }) { item -> display(item) }
    }
}
