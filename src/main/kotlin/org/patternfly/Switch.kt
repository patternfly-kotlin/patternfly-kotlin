package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.states
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.patternfly.dom.Id

// ------------------------------------------------------ dsl

/**
 * Creates a [Switch] component.
 *
 * @param value the value of the switch
 * @param baseClass optional CSS class that should be applied to the component
 * @param id optional ID of the component
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.switch(
    value: Store<Boolean>,
    reversed: Boolean = false,
    withCheckIcon: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    content: Switch.() -> Unit = {}
): Input = Switch(
    value = value,
    reversed = reversed,
    withCheckIcon = withCheckIcon
).apply(content).render(this, baseClass, id)

// ------------------------------------------------------ tag

/**
 * PatternFly [switch](https://www.patternfly.org/v4/components/switch/design-guidelines) component.
 *
 * A switch toggles the state of a setting (between on and off). Switches and checkboxes can often be used interchangeably, but the switch provides a more explicit, visible representation on a setting.
 *
 * @sample org.patternfly.sample.SwitchSample.switch
 */
public open class Switch(
    private val value: Store<Boolean>,
    private val reversed: Boolean,
    private val withCheckIcon: Boolean
) : PatternFlyComponent<Input>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin(),
    WithTitle by TitleMixin() {

    private var disabled: Flow<Boolean> = emptyFlow()
    private var label: SubComponent<Span>? = null
    private var labelOff: SubComponent<Span>? = null
    private var inputContext: (Input.() -> Unit)? = null
    private lateinit var inputTag: Input

    /**
     * Disables / enables the switch according to [value].
     */
    public fun disabled(value: Boolean) {
        disabled = flowOf(value)
    }

    /**
     * Disables / enables the switch according to [value].
     */
    public fun disabled(value: Flow<Boolean>) {
        disabled = value
    }

    /**
     * Defines the label when the switch is on.
     */
    public fun label(
        baseClass: String? = null,
        id: String? = null,
        context: Span.() -> Unit = {}
    ) {
        this.label = SubComponent(baseClass, id, context)
    }

    /**
     * Defines the label when the switch is off (if not set this is the same as [label]).
     */
    public fun labelOff(
        baseClass: String? = null,
        id: String? = null,
        context: Span.() -> Unit = {}
    ) {
        this.labelOff = SubComponent(baseClass, id, context)
    }

    /**
     * Allows customization of the underlying input tag.
     *
     * @sample org.patternfly.sample.SwitchSample.input
     */
    public fun input(context: Input.() -> Unit) {
        inputContext = context
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): Input = with(context) {
        label(
            baseClass = classes {
                +"switch".component()
                +("reverse".modifier() `when` reversed)
            }
        ) {
            markAs(ComponentType.Switch)

            val inputId = id ?: Id.unique(ComponentType.Switch.id, "chk")
            val onId = label?.id ?: Id.unique(ComponentType.Switch.id, "on")
            val offId = labelOff?.id ?: Id.unique(ComponentType.Switch.id, "off")
            domNode.htmlFor = inputId

            inputTag = input(baseClass = classes("switch".component("input"), baseClass), id = inputId) {
                applyElement(this)
                applyEvents(this)
                type("checkbox")
                aria["labelledby"] = onId
                disabled(disabled)
                checked(value.data)
                changes.states() handledBy value.update
                inputContext?.invoke(this)
            }

            span(baseClass = "switch".component("toggle")) {
                if (withCheckIcon) {
                    span(baseClass = "switch".component("toggle", "icon")) {
                        icon("check".fas())
                    }
                }
            }

            label?.let { lbl ->
                span(
                    baseClass = classes {
                        +"switch".component("label")
                        +"on".modifier()
                        +lbl.baseClass
                    },
                    id = onId
                ) {
                    aria["hidden"] = true
                    lbl.context(this)
                }
            }
            labelOff?.let { lbl ->
                span(
                    baseClass = classes {
                        +"switch".component("label")
                        +"off".modifier()
                        +lbl.baseClass
                    },
                    id = offId
                ) {
                    aria["hidden"] = true
                    lbl.context(this)
                }
            }
        }
        inputTag
    }
}
