package org.patternfly

import dev.fritz2.binding.Patch
import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.renderElement
import dev.fritz2.dom.mountDomNodePatch
import dev.fritz2.lenses.IdProvider
import dev.fritz2.utils.Myer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

@OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
internal fun <V, I> Flow<List<V>>.renderShifted(
    amount: Int,
    tag: Tag<Element>,
    idProvider: IdProvider<V, I>,
    content: RenderContext.(V) -> Tag<HTMLElement>
) {
    val jobs = mutableMapOf<Node, Job>()
    mountDomNodePatch(tag.job, tag.domNode,
        scan(Pair(emptyList<V>(), emptyList<V>()), { accumulator, newValue ->
            Pair(accumulator.second, newValue)
        }).flatMapConcat { (old, new) ->
            Myer.diff(old, new, idProvider)
        }.map { patch ->
            when (patch) {
                is Patch.Insert -> patch.copy(index = patch.index + amount)
                is Patch.InsertMany -> patch.copy(index = patch.index + amount)
                is Patch.Delete -> patch.copy(start = patch.start + amount)
                is Patch.Move -> patch.copy(from = patch.from + amount, to = patch.to + amount)
            }
        }.map { patch ->
            patch.map(tag.job) { value, newJob ->
                renderElement(newJob) {
                    content(value)
                }.also {
                    jobs[it.domNode] = newJob
                }
            }
        }) { node ->
        val job = jobs.remove(node)
        if (job != null) job.cancelChildren()
        else console.error("could not cancel renderEach-jobs!")
    }
}

