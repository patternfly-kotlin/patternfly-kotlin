package org.patternfly

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.lenses.IdProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.Element
import org.w3c.dom.Node

internal fun <V, I> Flow<List<V>>.renderShifted(
    amount: Int,
    tag: Tag<Element>,
    idProvider: IdProvider<V, I>,
    content: RenderContext.(V) -> RenderContext
) {
    val jobs = mutableMapOf<Node, Job>()
/*
    mountDomNodePatch(
        tag.job,
        tag.domNode,
        scan(Pair(emptyList(), emptyList()), ::accumulate).map { (old, new) ->
            Myer.diff(old, new, idProvider)
                .map { patch ->
                    when (patch) {
                        is Patch.Insert -> patch.copy(index = patch.index + amount)
                        is Patch.InsertMany -> patch.copy(index = patch.index + amount)
                        is Patch.Delete -> patch.copy(start = patch.start + amount)
                        is Patch.Move -> patch.copy(from = patch.from + amount, to = patch.to + amount)
                    }
                }.map { patch ->
                    patch.map(tag.job) { value, newJob ->
                        registerSingle(newJob, tag.unsafeCast<RenderContext>()) {
                            content(value)
                        }.also {
                            jobs[it.domNode] = newJob
                        }
                    }
                }
        }
    ) { node ->
        val job = jobs.remove(node)
        if (job != null) job.cancelChildren()
        else console.error("could not cancel renderEach-jobs!")
    }
*/
}

private fun <T> accumulate(
    accumulator: Pair<List<T>, List<T>>,
    newValue: List<T>
): Pair<List<T>, List<T>> = Pair(accumulator.second, newValue)
