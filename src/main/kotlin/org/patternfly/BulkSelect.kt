package org.patternfly

import dev.fritz2.dom.states
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.patternfly.dom.plusAssign

public fun <T> ToolbarItem.bulkSelect(
    itemsStore: SelectableItemPageContents<T>,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<PreSelection>.() -> Unit = {}
): Dropdown<PreSelection> {
    domNode.classList += "bulk-select".modifier()
    return dropdown(id = id, baseClass = baseClass) {
        checkboxToggle {
            text {
                itemsStore.selected.map {
                    if (it == 0) "" else "$it selected"
                }.asText()
            }
            checkbox {
                triState(
                    itemsStore.selectedItemsState.map {
                        when(it) {
                            SelectedItemsState.NoItems -> TriState.OFF
                            SelectedItemsState.AllItems -> TriState.ON
                            is SelectedItemsState.SelectedItems -> TriState.INDETERMINATE
                        }
                    }
                )
                changes.states().filter { !it }.map { } handledBy itemsStore.selectNone
                changes.states().filter { it }.map { } handledBy itemsStore.selectAll
            }
        }
        display { +it.text }
        items {
            PreSelection.values().map { item(it) }
        }
        store.singleSelection.unwrap() handledBy itemsStore.preSelect
        content(this)
    }
}
