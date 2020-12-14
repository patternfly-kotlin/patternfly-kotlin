package org.patternfly

import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.plusAssign
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.HTMLLabelElement

// TODO Document me
// ------------------------------------------------------ dsl

public fun RenderContext.switch(
    id: String? = null,
    baseClass: String? = null,
    content: Switch.() -> Unit = {}
): Switch = register(Switch(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

public class Switch internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLLabelElement>, Label(id = id, baseClass = classes(ComponentType.Switch, baseClass), job) {

    public val input: Input
    private val toggleTag: Span
    private val labelTag: Span
    private val labelOffTag: Span

    init {
        markAs(ComponentType.Switch)
        val inputId = Id.unique(ComponentType.Switch.id, "chk")
        val onId = Id.unique(ComponentType.Switch.id, "on")
        val offId = Id.unique(ComponentType.Switch.id, "off")
        domNode.htmlFor = inputId
        input = input(id = inputId, baseClass = "switch".component("input")) {
            type("checkbox")
            aria["labelledby"] = onId
        }
        toggleTag = span(baseClass = "switch".component("toggle")) {
            span(baseClass = "switch".component("toggle", "icon")) {
                icon("check".fas())
            }
        }
        labelTag = span(id = onId, baseClass = "switch".component("label")) {
            domNode.classList += "on".modifier()
            aria["hidden"] = true
        }
        labelOffTag = span(id = offId, baseClass = "switch".component("label")) {
            domNode.classList += "off".modifier()
            aria["hidden"] = true
        }
    }

    public fun label(value: String) {
        with(labelTag) {
            +value
        }
    }

    public fun label(value: Flow<String>) {
        with(labelTag) {
            value.asText()
        }
    }

    public fun labelOff(value: String) {
        with(labelOffTag) {
            +value
        }
    }

    public fun labelOff(value: Flow<String>) {
        with(labelOffTag) {
            value.asText()
        }
    }

    public fun disabled(value: Boolean) {
        input.disabled(value)
    }

    public fun disabled(value: Flow<Boolean>) {
        input.disabled(value)
    }
}
