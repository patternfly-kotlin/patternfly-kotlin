package org.patternfly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.checkAll
import io.kotest.property.forAll

open class ItemsTests : FunSpec({

    test("New Items") {
        with(Items<Int>({ it.toString() })) {
            all.shouldBeEmpty()
            items.shouldBeEmpty()
            filters.shouldBeEmpty()
            selected.shouldBeEmpty()
            sortInfo shouldBe null
        }
    }

    test("Add items") {
        checkAll(Arb.positiveInts(100)) { size ->
            val numbers = (0 until size).toList()
            with(Items<Int>({ it.toString() }).addAll(numbers)) {
                all shouldContainInOrder numbers
                items shouldContainInOrder numbers
                filters.shouldBeEmpty()
                selected.shouldBeEmpty()
                sortInfo shouldBe null
            }
        }
    }

    test("Filter items") {
        val numbers = (1..10).toList()
        var numberItems = Items<Int>({ it.toString() }).addAll(numbers)

        numberItems = numberItems.addFilter("even") { it % 2 == 0 }
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder listOf(2, 4, 6, 8, 10)
            filters shouldContainKey "even"
        }

        numberItems = numberItems.addFilter("three") { it % 3 == 0 }
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder listOf(6)
            filters.shouldContainKeys("even", "three")
        }

        numberItems = numberItems.removeFilter("even")
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder listOf(3, 6, 9)
            filters shouldContainKey "three"
        }

        numberItems = numberItems.removeFilter("foo")
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder listOf(3, 6, 9)
            filters shouldContainKey "three"
        }

        numberItems = numberItems.removeFilter("three")
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder numbers
            filters.shouldBeEmpty()
        }
    }

    test("Sort items") {
        val numbers = listOf(2, 65, 7, 89, 33, 123, 38, 75)
        var numberItems = Items<Int>({ it.toString() }).addAll(numbers)

        val asc = SortInfo<Int>("nat", "Natural", comparator = naturalOrder())
        numberItems = numberItems.sortWith(asc)
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder listOf(2, 7, 33, 38, 65, 75, 89, 123)
            sortInfo shouldBe asc
        }

        val desc = asc.toggle()
        numberItems = numberItems.sortWith(desc)
        with(numberItems) {
            all shouldContainInOrder numbers
            items shouldContainInOrder listOf(123, 89, 75, 65, 38, 33, 7, 2)
            sortInfo shouldBe desc
        }
    }

    test("Select no items") {
        forAll(Arb.positiveInts(100)) { size ->
            val numbers = (0 until size).toList()
            with(Items<Int>({ it.toString() }).addAll(numbers).selectNone()) {
                selected.isEmpty()
            }
        }
    }

    test("Select page") {
        forAll(Arb.positiveInts(100), Arb.positiveInts(100)) { size, pageSize ->
            val numbers = (0 until size).toList()
            with(
                Items<Int>(idProvider = { it.toString() }, pageInfo = PageInfo(pageSize = pageSize))
                    .addAll(numbers)
                    .selectPage()
            ) {
                selected.size == if (size < pageSize) pageInfo.total else pageInfo.pageSize
            }
        }
    }

    test("Select all items") {
        forAll(Arb.positiveInts(100)) { size ->
            val numbers = (0 until size).toList()
            with(Items<Int>({ it.toString() }).addAll(numbers).selectAll()) {
                selected.size == items.size
            }
        }
    }

    test("Select items") {
        val numbers = (1..10).toList()
        var numberItems = Items<Int>({ it.toString() }).addAll(numbers)

        numberItems = numberItems.select(2, true)
        with(numberItems) {
            selected.size shouldBe 1
            selected shouldContain "2"
        }

        numberItems = numberItems.select(3, true)
        with(numberItems) {
            selected.size shouldBe 2
            selected shouldContain "2"
            selected shouldContain "3"
        }

        numberItems = numberItems.select(2, false)
        with(numberItems) {
            selected.size shouldBe 1
            selected shouldContain "3"
        }
    }

    test("Toggle selection") {
        val numbers = (1..10).toList()
        var numberItems = Items<Int>({ it.toString() }).addAll(numbers)

        numberItems = numberItems.toggleSelection(5)
        with(numberItems) {
            selected.size shouldBe 1
            selected shouldContain "5"
        }

        numberItems = numberItems.toggleSelection(6)
        with(numberItems) {
            selected.size shouldBe 2
            selected shouldContain "5"
            selected shouldContain "6"
        }

        numberItems = numberItems.toggleSelection(5)
        with(numberItems) {
            selected.size shouldBe 1
            selected shouldContain "6"
        }
    }
})
