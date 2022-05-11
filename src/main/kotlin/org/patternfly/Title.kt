package org.patternfly

import dev.fritz2.dom.html.H
import dev.fritz2.dom.html.RenderContext
import org.patternfly.Level.H1
import org.patternfly.Level.H2
import org.patternfly.Level.H3
import org.patternfly.Level.H4
import org.patternfly.Level.H5
import org.patternfly.Level.H6

// ------------------------------------------------------ factory

/**
 * Creates a [Title] component.
 *
 * @param level the level of the heading
 * @param size the size of the heading
 * @param title the title of the heading
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.title(
    level: Level = H1,
    size: Size = level.size,
    title: String? = null,
    baseClass: String? = null,
    id: String? = null,
    context: Title.() -> Unit = {}
) {
    Title(level, size, title).apply(context).render(this, baseClass, id)
}

// ------------------------------------------------------ component

/**
 * PatternFly [title](https://www.patternfly.org/v4/components/title/design-guidelines/) component.
 *
 * A title component applies top and bottom margins, font-weight, font-size, and line-height to titles.
 *
 * @sample org.patternfly.sample.TitleSample.title
 */
public open class Title(private val level: Level, private val size: Size, title: String?) :
    PatternFlyComponent<Unit>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    private var content: (H.() -> Unit)? = null

    init {
        title?.let { title(it) }
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?) {
        with(context) {
            val classes = classes {
                +ComponentType.Title
                +size.modifier
                +baseClass
            }
            val h = when (level) {
                H1 -> h1(baseClass = classes, id = id) {}
                H2 -> h2(baseClass = classes, id = id) {}
                H3 -> h3(baseClass = classes, id = id) {}
                H4 -> h4(baseClass = classes, id = id) {}
                H5 -> h5(baseClass = classes, id = id) {}
                H6 -> h6(baseClass = classes, id = id) {}
            }
            with(h) {
                markAs(ComponentType.Title)
                applyElement(this)
                applyEvents(this)
                applyTitle(this)
            }
        }
    }
}

/**
 * Heading level used for the [Title] component.
 */
@Suppress("MagicNumber")
public enum class Level(public val level: Int, public val size: Size) {
    H1(1, Size.XL_2),
    H2(2, Size.XL),
    H3(3, Size.LG),
    H4(4, Size.MD),
    H5(5, Size.MD),
    H6(6, Size.MD),
}
