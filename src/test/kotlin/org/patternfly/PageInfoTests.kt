package org.patternfly

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.negativeInts
import io.kotest.property.arbitrary.positiveInts
import io.kotest.property.checkAll
import kotlin.math.min

@Suppress("EmptyRange", "unused")
class PageInfoTests : StringSpec({

    "new page info should be empty" {
        with(PageInfo()) {
            range shouldBe 0..0
            pages shouldBe 1
            firstPage shouldBe true
            lastPage shouldBe true
        }
    }

    "using an illegal page size should result in an exception" {
        checkAll(Arb.int(Int.MIN_VALUE..0)) { pageSize ->
            shouldThrow<IllegalArgumentException> {
                PageInfo(pageSize = pageSize)
            }
        }
    }

    "using an illegal page should result in an exception" {
        checkAll(Arb.negativeInts()) { page ->
            shouldThrow<IllegalArgumentException> {
                PageInfo(page = page)
            }
        }
    }

    "using an illegal total should result in an exception" {
        checkAll(Arb.negativeInts()) { total ->
            shouldThrow<IllegalArgumentException> {
                PageInfo(total = total)
            }
        }
    }

    "using valid ranges and pages should work" {
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

    "going to the first page should work" {
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

    "going to the last page should work" {
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

    "going to a specific page should work" {
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

    "changing the page size should work" {
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

    "changing the total number of items should work" {
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
