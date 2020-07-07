package org.patternfly.showcase

import dev.fritz2.dom.Tag
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.render
import org.w3c.dom.HTMLElement

interface Presenter<V : View> {
    val token: String
    val view: V

    /** Called once the presenter is created. */
    fun bind() {}

    /** Called before the presenter is shown. */
    fun prepareFromRequest(place: PlaceRequest) {}

    /** Called after the view has been attached to the DOM. */
    fun show() {}

    /** Called before the view has been removed from the DOM. */
    fun hide() {}

    companion object {
        private val registry: MutableMap<String, () -> Presenter<out View>> = mutableMapOf()
        private val instances: MutableMap<String, Presenter<out View>> = mutableMapOf()

        fun register(token: String, presenter: () -> Presenter<out View>) {
            registry[token] = presenter
        }

        operator fun contains(token: String): Boolean = token in registry

        @Suppress("UNCHECKED_CAST")
        fun <P : Presenter<out View>> lookup(token: String): P? {
            return if (token in instances) {
                instances[token] as P
            } else {
                if (token in registry) {
                    registry[token]?.invoke()?.let {
                        instances[token] = it
                        it.bind()
                        it as P
                    }
                } else {
                    null
                }
            }
        }
    }
}

interface View {
    val elements: List<Tag<HTMLElement>>
}

fun renderAll(vararg content: HtmlElements.() -> Tag<HTMLElement>): List<Tag<HTMLElement>> =
    content.map {
        render {
            it(this)
        }
    }
