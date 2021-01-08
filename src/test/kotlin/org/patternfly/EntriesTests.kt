package org.patternfly

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.patternfly.ItemSelection.MULTIPLE
import org.patternfly.ItemSelection.SINGLE
import org.patternfly.ItemSelection.SINGLE_PER_GROUP

@Suppress("unused")
class EntriesTests : FunSpec({

    test("Empty entries") {
        with(Entries<Int>({ it.toString() }, SINGLE, emptyList())) {
            all.shouldBeEmpty()
            entries.shouldBeEmpty()
            groups.shouldBeEmpty()
            items.shouldBeEmpty()
            selection.shouldBeEmpty()
            singleSelection shouldBe null
        }
    }

    // ------------------------------------------------------ items

    test("Filter items") {
        val even = numbers(1..10, SINGLE).filter { it % 2 == 0 }
        even.all.size shouldBe 10
        even.entries.size shouldBe 5
        even.items.unwrap().shouldContainAll(2, 4, 6, 8, 10)

        val threes = numbers(1..10, SINGLE).filter { it % 3 == 0 }
        threes.all.size shouldBe 10
        threes.entries.size shouldBe 3
        threes.items.unwrap().shouldContainAll(3, 6, 9)

        val numbers = threes.clearFilter()
        numbers.all.size shouldBe 10
        numbers.entries.size shouldBe 10
        numbers.items.unwrap().shouldContainAll((1..10).toList())
    }

    test("Select single item") {
        var numbers = numbers(1..3, SINGLE)
        for (i in 1..3) {
            numbers = numbers.select(i)
            numbers.selection.size shouldBe 1
            numbers.selection.first().unwrap() shouldBe i
            numbers.singleSelection?.unwrap() shouldBe i
        }
    }

    test("Select multiple items") {
        var numbers = numbers(1..3, MULTIPLE)
        for (i in 1..3) {
            numbers = numbers.select(i)
            numbers.selection.size shouldBe i
            numbers.selection.unwrap().shouldContainAll((1..i).toList())
        }
    }

    // ------------------------------------------------------ groups

    test("Just some groups") {
        val groups = groups(1..12, 3, SINGLE)
        groups.all.size shouldBe 4
        groups.entries.size shouldBe 4
        groups.groups.size shouldBe 4
        groups.items.size shouldBe 12

        groups.groups[0].items.unwrap().shouldContainAll(1, 2, 3)
        groups.groups[1].items.unwrap().shouldContainAll(4, 5, 6)
        groups.groups[2].items.unwrap().shouldContainAll(7, 8, 9)
        groups.groups[3].items.unwrap().shouldContainAll(10, 11, 12)
    }

    test("Filter items in groups") {
        val groups = groups(1..12, 3, SINGLE).filter { it in 8..11 }
        groups.all.size shouldBe 4
        groups.entries.size shouldBe 2
        groups.groups.size shouldBe 2
        groups.items.size shouldBe 4

        groups.groups[0].items.unwrap().shouldContainAll(8, 9)
        groups.groups[1].items.unwrap().shouldContainAll(10, 11)
    }

    test("Select single item across all groups") {
        var groups = groups(1..12, 3, SINGLE)
        for (i in 1..12) {
            groups = groups.select(i)
            groups.selection.size shouldBe 1
            groups.selection.first().unwrap() shouldBe i
            groups.singleSelection?.unwrap() shouldBe i
        }
    }

    test("Select single item per group") {
        var groups = groups(1..12, 3, SINGLE_PER_GROUP)
        for (i in 1..12) {
            groups = groups.select(i)
            when (i) {
                in 1..3 -> {
                    groups.selection.size shouldBe 1
                    groups.selection.unwrap().shouldContainAll(i)
                }
                in 4..6 -> {
                    groups.selection.size shouldBe 2
                    groups.selection.unwrap().shouldContainAll(3, i)
                }
                in 7..9 -> {
                    groups.selection.size shouldBe 3
                    groups.selection.unwrap().shouldContainAll(3, 6, i)
                }
                in 10..12 -> {
                    groups.selection.size shouldBe 4
                    groups.selection.unwrap().shouldContainAll(3, 6, 9, i)
                }
                else -> {
                    fail("The impossible just happened!")
                }
            }
        }
    }

    test("Select multiple items with groups") {
        var groups = groups(1..12, 3, MULTIPLE)
        for (i in 1..12) {
            groups = groups.select(i)
            groups.selection.size shouldBe i
            groups.selection.unwrap().shouldContainAll((1..i).toList())
        }
    }
})

private fun numbers(range: IntRange, itemSelection: ItemSelection) =
    items<Int>({ it.toString() }, itemSelection) {
        range.forEach { item(it) }
    }

private fun groups(range: IntRange, step: Int, itemSelection: ItemSelection) =
    groups<Int>({ it.toString() }, itemSelection) {
        range.chunked(step).forEach { group ->
            group {
                group.forEach { item(it) }
            }
        }
    }
