package org.patternfly

import dev.fritz2.binding.Patch
import dev.fritz2.dom.MultipleRootElementsException
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.mountDomNodePatch
import dev.fritz2.lenses.IdProvider
import dev.fritz2.utils.Myer
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.Node

internal fun <V, I> Flow<List<V>>.renderShifted(
    amount: Int,
    tag: Tag<Element>,
    idProvider: IdProvider<V, I>,
    content: RenderContext.(V) -> RenderContext
) {
    val jobs = mutableMapOf<Node, Job>()
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
}

private inline fun registerSingle(
    job: Job,
    parent: RenderContext,
    content: RenderContext.() -> RenderContext
): WithDomNode<HTMLElement> = content(
    object : RenderContext(
        "",
        parent.id,
        parent.baseClass,
        job,
        Scope(),
        parent.domNode.unsafeCast<HTMLElement>()
    ) {
        var alreadyRegistered: Boolean = false

        override fun <E : Element, W : WithDomNode<E>> register(element: W, content: (W) -> Unit): W {
            if (alreadyRegistered) {
                throw MultipleRootElementsException("You can have only one root-tag per html-context!")
            } else {
                content(element)
                alreadyRegistered = true
                return element
            }
        }
    }
)

private fun <T> accumulate(
    accumulator: Pair<List<T>, List<T>>,
    newValue: List<T>
): Pair<List<T>, List<T>> = Pair(accumulator.second, newValue)
