package org.patternfly

import dev.fritz2.dom.states
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.patternfly.dom.plusAssign

public fun <T> ToolbarItem.bulkSelect(
    itemStore: ItemStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<PreSelection>.() -> Unit = {}
): Dropdown<PreSelection> {
    domNode.classList += "bulk-select".modifier()
    return dropdown(id = id, baseClass = baseClass) {
        checkboxToggle {
            text {
                itemStore.selected.map {
                    if (it == 0) "" else "$it selected"
                }.asText()
            }
            checkbox {
                triState(
                    itemStore.data.map {
                        when {
                            it.selected.isEmpty() -> TriState.OFF
                            it.selected.size == it.items.size -> TriState.ON
                            else -> TriState.INDETERMINATE
                        }
                    }
                )
                changes.states().filter { !it }.map { } handledBy itemStore.selectNone
                changes.states().filter { it }.map { } handledBy itemStore.selectAll
            }
        }
        display { +it.text }
        items {
            PreSelection.values().map { item(it) }
        }
        store.selects.unwrap() handledBy itemStore.preSelect
        content(this)
    }
}
