package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import org.patternfly.dom.Id
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

public fun <T> RenderContext.actionMenu(
    store: MenuStore<T> = MenuStore(itemSelection = ItemSelection.SINGLE),
    id: String? = null,
    baseClass: String? = null,
    content: Menu<T>.() -> Unit = {}
): Menu<T> = register(Menu(store, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [menu](https://www.patternfly.org/v4/components/menu/design-guidelines) component.
 *
 * A menu is a list of options or actions that users can choose from. It can be used in a variety of contexts whenever the user needs to choose between multiple values, options, or actions. A menu can be opened in a [Dropdown] or select list, or it can be revealed by right clicking on a specific region within a page.
 */
public class Menu<T> internal constructor(
    public val store: MenuStore<T>,
    id: String?,
    baseClass: String?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    Div(id = id, baseClass = classes(ComponentType.Menu, baseClass), job) {

    init {
        markAs(ComponentType.Menu)
    }
}

// ------------------------------------------------------ store

/**
 * An [EntriesStore] with the specified selection mode.
 */
public class MenuStore<T>(
    idProvider: IdProvider<T, String> = { Id.build(it.toString()) },
    itemSelection: ItemSelection
) : EntriesStore<T>(idProvider, itemSelection)
