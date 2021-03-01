package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

// ------------------------------------------------------ dsl

/**
 * Creates a [Skeleton] component.
 *
 * @param id optional id of the element
 * @param baseClass optional CSS class(es) that should be applied to the element
 * @param fontSize optional fontSize of the component
 * @param height optional height of the component
 * @param width optional width of the component
 * @param shape optional shape of the component
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.skeleton(
    id: String? = null,
    baseClass: String? = null,
    fontSize: FontSize? = null,
    height: Height? = null,
    width: Width? = null,
    shape: Shape? = null,
    content: Skeleton.() -> Unit = {},
): Skeleton = register(
    Skeleton(
        id = id,
        baseClass = baseClass,
        fontSize = fontSize,
        height = height,
        width = width,
        shape = shape,
        job
    ),
    content
)

// ------------------------------------------------------ tag

/**
 * PatternFly [skeleton](https://www.patternfly.org/v4/components/skeleton) component.
 *
 * A skeleton is a type of loading state that allows you to expose content incrementally. For content that may take a
 * long time to load, use a [progress bar](https://www.patternfly.org/v4/components/progress) in place of a skeleton.
 *
 * @sample org.patternfly.sample.SkeletonSample.skeletons
 */
public class Skeleton internal constructor(
    id: String?,
    baseClass: String?,
    fontSize: FontSize?,
    height: Height?,
    width: Width?,
    shape: Shape?,
    job: Job
) : PatternFlyComponent<HTMLDivElement>,
    Div(
        id = id,
        baseClass = classes {
            +ComponentType.Skeleton
            +baseClass
            +fontSize?.modifier
            +height?.modifier
            +width?.modifier
            +shape?.modifier
        },
        job
    ) {

    /**
     * Override the default text content by creating an accessible SPAN which contains the given text.
     */
    override fun String.unaryPlus(): Node {
        val screenReaderElement = span(baseClass = screenReader()) {
            +this@unaryPlus
        }
        return domNode.appendChild(screenReaderElement.domNode)
    }

    init {
        markAs(ComponentType.Skeleton)
    }
}
