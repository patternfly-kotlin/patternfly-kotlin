package org.patternfly

import org.patternfly.dom.plusAssign

public fun <T> ToolbarItem.bulkSelect(
    itemsStore: ItemsStore<T>,
    id: String? = null,
    baseClass: String? = null,
    content: Dropdown<PreSelection>.() -> Unit = {}
) {
    domNode.classList += "bulk-select".modifier()
    dropdown<PreSelection>(id = id, baseClass = baseClass) {
        toggle {
            checkbox {
//                text {
//                    itemsStore.selected.map {
//                        if (it == 0) "" else "$it selected"
//                    }.asText()
//                }
//                triState(
//                    itemsStore.data.map {
//                        when {
//                            it.selected.isEmpty() -> TriState.OFF
//                            it.selected.size == it.items.size -> TriState.ON
//                            else -> TriState.INDETERMINATE
//                        }
//                    }
//                )
                events {
//                    changes.states().filter { !it }.map { } handledBy itemsStore.selectNone
//                    changes.states().filter { it }.map { } handledBy itemsStore.selectAll
                }
            }
        }
        PreSelection.values().forEach { ps ->
            item(ps) {
                +ps.text
            }
        }
        content(this)
    }
}
