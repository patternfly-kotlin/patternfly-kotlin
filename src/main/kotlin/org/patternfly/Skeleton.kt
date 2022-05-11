package org.patternfly

import dev.fritz2.dom.html.RenderContext

// ------------------------------------------------------ factory

/**
 * Creates a [Skeleton] component.
 *
 * @param shape optional shape of the skeleton
 * @param width optional width of the skeleton
 * @param height optional height of the skeleton
 * @param textSize optional fontSize of the skeleton
 * @param title optional screen reader text
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.skeleton(
    shape: Shape? = null,
    width: String? = null,
    height: String? = null,
    textSize: TextSize? = null,
    title: String? = null,
    baseClass: String? = null,
    id: String? = null,
    context: Skeleton.() -> Unit = {},
) {
    Skeleton(shape, width, height, textSize, title).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [skeleton](https://www.patternfly.org/v4/components/skeleton/design-guidelines) component.
 *
 * A skeleton is a type of loading state that allows you to expose content incrementally. For content that may take a long time to load, use a progress bar in place of a skeleton.
 *
 * @sample org.patternfly.sample.SkeletonSample.skeletons
 */
public open class Skeleton(
    private var shape: Shape?,
    private var width: String?,
    private var height: String?,
    private var textSize: TextSize?,
    title: String?
) : PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    init {
        title?.let { title(it) }
    }

    public fun shape(shape: Shape) {
        this.shape = shape
    }

    public fun width(width: String) {
        this.width = width
    }

    public fun height(height: String) {
        this.height = height
    }

    public fun textSize(textSize: TextSize) {
        this.textSize = textSize
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            div(
                baseClass = classes {
                    +ComponentType.Skeleton
                    +textSize?.modifier
                    +shape?.modifier
                    +baseClass
                },
                id = id
            ) {
                markAs(ComponentType.Skeleton)
                val style = buildString {
                    width?.let { append("--pf-c-skeleton--Width:$it;") }
                    height?.let { append("--pf-c-skeleton--Height:$it;") }
                }
                if (style.isNotEmpty()) {
                    inlineStyle(style)
                }
                applyElement(this)
                applyEvents(this)
                if (hasTitle) {
                    span(baseClass = "screen-reader".util()) { applyTitle(this) }
                }
            }
        }
    }
}

/**
 * Shape modifier for the [Skeleton] component.
 */
public enum class Shape(public val modifier: String) {
    CIRCLE("circle".modifier()),
    SQUARE("square".modifier()),
}
