package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.patternfly.dom.showIf

// ------------------------------------------------------ factory

/**
 * Creates a new [OptionsMenu] component.
 *
 * @param grouped whether the options menu contains groups
 * @param align the alignment of the options menu
 * @param up controls the direction of the options menu
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.optionsMenu(
    grouped: Boolean = false,
    align: Align? = null,
    up: Boolean = false,
    closeOnSelect: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: OptionsMenu.() -> Unit = {}
) {
    OptionsMenu(
        grouped = grouped,
        align = align,
        up = up,
        closeOnSelect = closeOnSelect
    ).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [options menu](https://www.patternfly.org/v4/components/options-menu/design-guidelines) component.
 *
 * An options menu is similar to a dropdown, but provides a way to select among a set of optional settings rather than trigger an action. An options menu consists of a [toggle][OptionsMenuToggle] to open and close a menu of [entries][Entry].
 *
 * You can choose between different toggle variations:
 * - [text toggle][OptionsMenuToggle.text]
 * - [icon toggle][OptionsMenuToggle.icon]
 * - [kebab toggle][OptionsMenuToggle.kebab]
 *
 * The [options menu entries][Entry] can be added statically or by using a store. See the samples for more details.
 *
 * @sample org.patternfly.sample.OptionsMenuSample.staticEntries
 * @sample org.patternfly.sample.OptionsMenuSample.dynamicEntries
 */
@Suppress("TooManyFunctions")
public class OptionsMenu(grouped: Boolean, align: Align?, up: Boolean, private val closeOnSelect: Boolean) :
    EntriesComponent<OptionsMenuToggle, OptionsMenuItem>(
        ComponentType.OptionsMenu,
        "options-menu",
        grouped,
        align,
        up,
        ::OptionsMenuItem
    ) {

    internal val defaultSelectionStore: RootStore<String?> = storeOf(null)
    override val toggle: OptionsMenuToggle = OptionsMenuToggle(TextToggleKind(null, null) {}, expandedStore)

    override fun renderItem(context: RenderContext, entry: Entry): RenderContext =
        with(context) {
            li {
                if (entry is OptionsMenuItem) {
                    attr("role", "menuitem")
                    renderOptionsMenuItem(this, entry)
                } else {
                    unsupportedItem(context, this.domNode, entry)
                }
            }
        }

    private fun renderOptionsMenuItem(context: RenderContext, item: OptionsMenuItem): RenderContext =
        with(context) {
            button(
                baseClass = classes {
                    +"options-menu".component("menu", "item")
                    +("disabled".modifier() `when` item.disabled)
                }
            ) {
                attr("tabindex", "-1")
                if (item.disabled) {
                    aria["disabled"] = true
                    attr("disabled", "true")
                }
                if (closeOnSelect) {
                    clicks handledBy expandedStore.collapse
                }
                if (!item.customSelected) {
                    item.selected(defaultSelectionStore.data.map { it == item.id })
                    clicks.map { item.id } handledBy defaultSelectionStore.update
                }

                item.applyEvents(this)
                item.applyTitle(this)
                span(baseClass = "options-menu".component("menu", "item", "icon")) {
                    showIf(item.selected)
                    icon("check".fas())
                }
            }
        }
}

// ------------------------------------------------------ toggle

/**
 * The options menu toggle. The options menu component supports the following toggles:
 *
 * - [text toggle][Toggle.text]
 * - [icon toggle][Toggle.icon]
 * - [kebab toggle][Toggle.kebab]
 */
public class OptionsMenuToggle internal constructor(kind: ToggleKind, expandedStore: ExpandedStore) :
    Toggle(ComponentType.OptionsMenu, "options-menu", kind, expandedStore) {

    override fun setupToggleButton(button: Button) {
        super.setupToggleButton(button)
        with(button) {
            aria["haspopup"] = "listbox"
        }
    }
}

// ------------------------------------------------------ entries

public class OptionsMenuItem internal constructor(id: String, title: String?) :
    Item<OptionsMenuItem>(id, title) {

    internal var customSelected: Boolean = false
    internal var selected: Flow<Boolean> = flowOf(false)

    public fun selected(selected: Flow<Boolean>) {
        this.selected = selected
        this.customSelected = true
    }
}
