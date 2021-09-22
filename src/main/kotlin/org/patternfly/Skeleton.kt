package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.patternfly.component.PatternFlyComponent2
import org.patternfly.component.markAs
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

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

// ------------------------------------------------------ component

public fun RenderContext.skeleton2(baseClass: String? = null, id: String? = null, build: Skeleton2.() -> Unit) {
    Skeleton2().apply(build).render(this, baseClass, id)
}

public class Skeleton2 : PatternFlyComponent2<Unit> {

    private var fontSize: FontSize? = null
    private var height: Height? = null
    private var width: Width? = null
    private var shape: Shape? = null
    private var text: (RenderContext.() -> Unit)? = null

    public fun fontSize(fontSize: FontSize) {
        this.fontSize = fontSize
    }

    public fun height(height: Height) {
        this.height = height
    }

    public fun width(width: Width) {
        this.width = width
    }

    public fun shape(shape: Shape) {
        this.shape = shape
    }

    public fun text(value: String) {
        this.text(flowOf(value))
    }

    public fun text(value: Flow<String>) {
        text = {
            span(baseClass = screenReader()) {
                value.asText()
            }
        }
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.Skeleton
                    +fontSize?.modifier
                    +height?.modifier
                    +width?.modifier
                    +shape?.modifier
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Skeleton)
                text?.invoke(this)
            }
        }
    }
}

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
    WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(
        id = id,
        baseClass = classes {
            +ComponentType.Skeleton
            +fontSize?.modifier
            +height?.modifier
            +width?.modifier
            +shape?.modifier
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Skeleton)
    }

    override fun delegate(): HTMLSpanElement {
        return span(baseClass = screenReader()) {}.domNode
    }
}
