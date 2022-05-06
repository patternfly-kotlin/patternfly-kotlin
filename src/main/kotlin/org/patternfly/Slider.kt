package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.math.abs
import kotlin.math.roundToInt

// ------------------------------------------------------ factory

/**
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    progression: IntProgression = 0..100 step 10,
    steps: List<Step> = listOf(),
    hideBoundaries: Boolean = false,
    hideSteps: Boolean = false,
    hideTicks: Boolean = false,
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value,
    progression,
    steps,
    hideBoundaries,
    hideSteps,
    hideTicks
).apply(context).render(this, baseClass, id)

// ------------------------------------------------------ component

/**
 * PatternFly [slider](https://www.patternfly.org/v4/components/slider/design-guidelines) component.
 *
 * A slider provides a quick and effective way for users to set and adjust a numeric value from a defined range of values.
 */
public open class Slider(
    private var value: Store<Int>,
    private val progression: IntProgression,
    steps: List<Step>,
    private val hideBoundaries: Boolean,
    private val hideSteps: Boolean,
    private val hideTicks: Boolean
) : PatternFlyComponent<Slider>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var disabled: Flow<Boolean> = emptyFlow()

    private val steps: List<Step> = steps.ifEmpty {
        progression.map { Step(it, it.toString()) }
    }

    private val range: IntRange =
        if (steps.isEmpty()) progression.first..progression.last else steps.first().value..steps.last().value

    init {
        if (value.current !in range) {
            value.update(value.current.coerceIn(range))
        }
    }

    /**
     * Disables the slider.
     */
    public fun disabled(value: Boolean) {
        disabled = flowOf(value)
    }

    /**
     * Disables the slider based on the values in the specified [Flow].
     */
    public fun disabled(value: Flow<Boolean>) {
        disabled = value
    }

    public fun value(value: Int) {
        this.value.update(value.coerceIn(range))
    }

    public fun value(value: Store<Int>) {
        this.value = value
        if (this.value.current !in range) {
            this.value.update(this.value.current.coerceIn(range))
        }
    }

    override fun render(context: RenderContext, baseClass: String?, id: String?): Slider = with(context) {
        div(baseClass = classes(ComponentType.Slider, baseClass), id = id) {
            markAs(ComponentType.Slider)
            applyElement(this)
            applyEvents(this)
            classMap(disabled.map { mapOf("disabled".modifier() to it) })
            inlineStyle(value.data.map { "--pf-c-slider--value: ${percent(it)}%" })

            div(baseClass = "slider".component("main")) {
                div(baseClass = "slider".component("rail")) {
                    div(baseClass = "slider".component("rail-track")) {}
                }
                div(baseClass = "slider".component("steps")) {
                    aria["hidden"] = true
                    for ((index, step) in steps.withIndex()) {
                        div(baseClass = "slider".component("step")) {
                            classMap(value.data.map { mapOf("active".modifier() to (it >= step.value)) })
                            inlineStyle("--pf-c-slider__step--Left: ${percent(step.value)}%")

                            if (!hideTicks) {
                                div(baseClass = "slider".component("step", "tick")) {}
                            }

                            val firstStep = index == 0
                            val lastStep = index == steps.size - 1
                            val showLabel = step.label.isNotEmpty() && if (firstStep || lastStep)
                                !hideBoundaries
                            else
                                !hideSteps
                            if (showLabel) {
                                div(baseClass = "slider".component("step", "label")) {
                                    +step.label
                                    inlineStyle("cursor:pointer")
                                    clicks.map { step.value } handledBy value.update
                                }
                            }
                        }
                    }
                }
                div(baseClass = "slider".component("thumb")) {
                    aria["valuemin"] = 0
                    aria["valuemin"] = 8
                    attr("aria-valuenow", value)
                    attr("role", "slider")
                    attr("tabindex", 0)
                }
            }
        }
        this@Slider
    }

    private fun percent(current: Int): Double {
        val diff = abs(range.first)
        val percent = (current + diff).toDouble() / (range.last + diff).toDouble() * 100
        return (percent * 100.0).roundToInt() / 100.0 // round to two decimal places
    }
}

// ------------------------------------------------------ steps & store

public data class Step(val value: Int, val label: String = "")