package org.patternfly

import dev.fritz2.binding.Patch
import dev.fritz2.binding.Seq
import kotlinx.coroutines.flow.map

public fun <T> Seq<T>.shift(amount: Int): Seq<T> = Seq(this.data.map { patch ->
    when (patch) {
        is Patch.Insert -> patch.copy(index = patch.index + amount)
        is Patch.InsertMany -> patch.copy(index = patch.index + amount)
        is Patch.Delete -> patch.copy(start = patch.start + amount)
        is Patch.Move -> patch.copy(from = patch.from + amount, to = patch.to + amount)
    }
})
