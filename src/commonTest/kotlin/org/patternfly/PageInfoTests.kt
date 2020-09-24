package org.patternfly

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.checkAll
import kotlin.math.min

@Suppress("EmptyRange")
open class PageInfoTests : FunSpec({

    test("New page info") {
        with(PageInfo()) {
            range shouldBe 1..0
            pages shouldBe 1
            firstPage shouldBe true
            lastPage shouldBe true
        }
    }

    test("Valid range and pages") {
        checkAll(Arb.positiveInts(), Arb.positiveInts()) { pageSize, total ->
            with(PageInfo(pageSize = pageSize, total = total)) {
                range.first shouldBeGreaterThanOrEqual 1
                range.last shouldBeLessThanOrEqual total
                page shouldBeInRange (0 until pages)
                firstPage shouldBe (page == 0)
                lastPage shouldBe (page == pages - 1)
            }
        }
    }

    test("Goto first page") {
        checkAll(Arb.positiveInts(), Arb.positiveInts()) { pageSize, total ->
            val pageInfo = PageInfo(pageSize = pageSize, total = total).gotoFirstPage()
            with(pageInfo) {
                range.first shouldBe 1
                range.last shouldBe min(total, pageSize)
                page shouldBe 0
                firstPage shouldBe true
                lastPage shouldBe (pages == 1)
            }
        }
    }

    test("Goto last page") {
        checkAll(Arb.positiveInts(), Arb.positiveInts()) { pageSize, total ->
            val pageInfo = PageInfo(pageSize = pageSize, total = total).gotoLastPage()
            with(pageInfo) {
                range.first shouldBe (page * pageSize) + 1
                range.last shouldBe min(total, range.first + pageSize - 1)
                page shouldBe pages - 1
                firstPage shouldBe (pages == 1)
                lastPage shouldBe true
            }
        }
    }

    test("Goto page") {
        checkAll(Arb.positiveInts(), Arb.positiveInts(), Arb.int()) { pageSize, total, pg ->
            val pageInfo = PageInfo(pageSize = pageSize, total = total).gotoPage(pg)
            with(pageInfo) {
                range.first shouldBeGreaterThanOrEqual 1
                range.last shouldBeLessThanOrEqual total
                page shouldBeInRange (0 until pages)
                firstPage shouldBe (page == 0)
                lastPage shouldBe (page == pages - 1)
            }
        }
    }

    test("Change page size") {
        checkAll(Arb.positiveInts(), Arb.positiveInts(), Arb.positiveInts()) { pageSize, total, ps ->
            val pageInfo = PageInfo(pageSize = pageSize, total = total).pageSize(ps)
            with(pageInfo) {
                range.first shouldBeGreaterThanOrEqual 1
                range.last shouldBeLessThanOrEqual total
                page shouldBeInRange (0 until pages)
                firstPage shouldBe (page == 0)
                lastPage shouldBe (page == pages - 1)
            }
        }
    }

    test("Change total") {
        checkAll(Arb.positiveInts(), Arb.positiveInts(), Arb.positiveInts()) { pageSize, total, tt ->
            val pageInfo = PageInfo(pageSize = pageSize, total = total).total(tt)
            with(pageInfo) {
                range.first shouldBeGreaterThanOrEqual 1
                range.last shouldBeLessThanOrEqual tt
                page shouldBeInRange (0 until pages)
                firstPage shouldBe (page == 0)
                lastPage shouldBe (page == pages - 1)
            }
        }
    }
})
