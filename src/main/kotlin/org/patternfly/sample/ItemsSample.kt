@file:Suppress("UNUSED_VARIABLE")

package org.patternfly.sample

import org.patternfly.Items
import org.patternfly.SortInfo

internal class ItemsSample {

    fun page() {
        val items: Items<Int> = Items({ it.toString() }, (0 until 100).toList())
        val page: List<Int> = items.copy(pageInfo = items.pageInfo.gotoNextPage()).page
        // page = [10, 11, 12, 13, 14, 15, 16, 17, 18, 19]
    }

    fun filter() {
        val items: Items<Int> = Items({ it.toString() }, (0 until 10).toList())
        val even: List<Int> = items.addFilter("even") { it % 2 == 0 }.items
        // even = [0, 2, 4, 6, 8]
    }

    fun select() {
        val items: Items<Int> = Items({ it.toString() }, (0 until 10).toList())
        val selection: List<Int> = items.select(2, true).selection
        // selection = [2]
    }

    fun sort() {
        val items: Items<Int> = Items({ it.toString() }, (0 until 10).toList())
        val sortInfo = SortInfo<Int>("reversed", "Reversed", naturalOrder(), false)
        val reversed: List<Int> = items.sortWith(sortInfo).items
        // reversed = [9, 8, 7, 6, 5, 4, 3, 2, 1, 0]
    }
}
