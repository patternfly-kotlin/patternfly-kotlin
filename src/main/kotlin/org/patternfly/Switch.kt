package org.patternfly

import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.plusAssign
import org.w3c.dom.HTMLLabelElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Switch] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.switch(
    id: String? = null,
    baseClass: String? = null,
    content: Switch.() -> Unit = {}
): Switch = register(Switch(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * PatternFly [switch](https://www.patternfly.org/v4/components/switch/design-guidelines) component.
 *
 * A switch toggles the state of a setting (between on and off). Switches and checkboxes can often be used interchangeably, but the switch provides a more explicit, visible representation on a setting.
 *
 * @sample org.patternfly.sample.SwitchSample.switch
 */
public class Switch internal constructor(id: String?, baseClass: String?, job: Job) :
    PatternFlyComponent<HTMLLabelElement>, Label(id = id, baseClass = classes(ComponentType.Switch, baseClass), job) {

    private val toggleTag: Span
    private val labelTag: Span
    private val labelOffTag: Span

    /**
     * The underlying input tag. Use this property if you want to use the events of the input element.
     *
     * @sample org.patternfly.sample.SwitchSample.input
     */
    public val input: Input

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

    /**
     * Text value for the label when on
     */
    public fun label(value: String) {
        with(labelTag) {
            +value
        }
    }

    /**
     * Text value for the label when on
     */
    public fun label(value: Flow<String>) {
        with(labelTag) {
            value.asText()
        }
    }

    /**
     * Text value for the label when off
     */
    public fun labelOff(value: String) {
        with(labelOffTag) {
            +value
        }
    }

    /**
     * Text value for the label when off
     */
    public fun labelOff(value: Flow<String>) {
        with(labelOffTag) {
            value.asText()
        }
    }

    /**
     * Disables or enables the switch.
     */
    public fun disabled(value: Boolean) {
        input.disabled(value)
    }

    /**
     * Disables or enables the switch.
     */
    public fun disabled(value: Flow<Boolean>) {
        input.disabled(value)
    }
}
