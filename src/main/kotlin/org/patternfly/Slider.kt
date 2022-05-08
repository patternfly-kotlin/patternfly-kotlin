package org.patternfly

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
 * Creates a continuous slider using an int progression.
 * Boundaries are shown, steps and ticks are hidden by default.
 *
 * @param value the initial value
 * @param steps the possible values
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.SliderSample.intProgression
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    steps: IntProgression,
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value = value,
    steps = emptyList(),
    progression = Step(steps.first, steps.first.toString())..Step(steps.last, steps.last.toString()) step steps.step,
    showBoundaries = SHOW_PREDICATE,
    showSteps = HIDE_PREDICATE,
    showTicks = HIDE_PREDICATE
).apply(context).render(this, baseClass, id)

/**
 * Creates a continuous slider using a step progression.
 * The step progression can have custom labels for the boundaries and uses an int progression for the steps in between.
 * Boundaries are shown as defined by the steps, steps and ticks are hidden by default.
 *
 * @param value the initial value
 * @param steps the possible values
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.SliderSample.stepProgression
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    steps: StepProgression,
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value = value,
    steps = emptyList(),
    progression = steps,
    showBoundaries = LABEL_PREDICATE,
    showSteps = HIDE_PREDICATE,
    showTicks = HIDE_PREDICATE
).apply(context).render(this, baseClass, id)

/**
 * Creates a slider using custom steps.
 * Boundaries and steps are shown as defined by the custom steps, ticks are shown by default.
 *
 * @param value the initial value
 * @param steps the possible values
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.SliderSample.customSteps
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    steps: List<Step>,
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value = value,
    steps = steps,
    progression = StepProgression.EMPTY,
    showBoundaries = LABEL_PREDICATE,
    showSteps = LABEL_PREDICATE,
    showTicks = SHOW_PREDICATE
).apply(context).render(this, baseClass, id)

// ------------------------------------------------------ component

/**
 * PatternFly [slider](https://www.patternfly.org/v4/components/slider/design-guidelines) component.
 *
 * A slider provides a quick and effective way for users to set and adjust a numeric value from a defined range of values.
 */
public open class Slider(
    private var value: Store<Int>,
    private val steps: List<Step>,
    private val progression: StepProgression,
    private var showBoundaries: StepPredicate,
    private var showSteps: StepPredicate,
    private var showTicks: StepPredicate,
) : PatternFlyComponent<Slider>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private var disabled: Flow<Boolean> = emptyFlow()
    private val boundary: IntRange = if (steps.isNotEmpty())
        steps.first().value..steps.last().value
    else if (!progression.isEmpty())
        progression.values.first..progression.values.last
    else
        IntRange.EMPTY

    init {
        // make sure initial value is valid
        value.update(coerceIn(value.current))
    }

    public fun hideBoundaries() {
        boundaries(HIDE_PREDICATE)
    }

    public fun showBoundaries() {
        boundaries(SHOW_PREDICATE)
    }

    public fun boundaries(predicate: StepPredicate) {
        this.showBoundaries = predicate
    }

    public fun hideSteps() {
        steps(HIDE_PREDICATE)
    }

    public fun showSteps() {
        steps(SHOW_PREDICATE)
    }

    public fun steps(predicate: StepPredicate) {
        this.showSteps = predicate
    }

    public fun hideTicks() {
        ticks(HIDE_PREDICATE)
    }

    public fun showTicks() {
        ticks(SHOW_PREDICATE)
    }

    public fun ticks(predicate: StepPredicate) {
        this.showTicks = predicate
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
                    if (steps.isNotEmpty()) {
                        for ((index, step) in steps.withIndex()) {
                            val firstStep = index == 0
                            val lastStep = index == steps.size - 1
                            renderStep(this, step.value, step.label, firstStep, lastStep)
                        }
                    } else if (!progression.isEmpty()) {
                        for (step in progression.values) {
                            val firstStep = step == progression.values.first
                            val lastStep = step == progression.values.last
                            if (firstStep) {
                                renderStep(
                                    this, progression.first.value, progression.first.label, firstStep, lastStep
                                )
                            } else if (lastStep) {
                                renderStep(
                                    this, progression.last.value, progression.last.label, firstStep, lastStep
                                )
                            } else {
                                renderStep(this, step, step.toString(), firstStep, lastStep)
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

    private fun renderStep(context: RenderContext, step: Int, label: String, firstStep: Boolean, lastStep: Boolean) {
        with(context) {
            div(baseClass = "slider".component("step")) {
                classMap(value.data.map { mapOf("active".modifier() to (it >= step)) })
                inlineStyle("--pf-c-slider__step--Left: ${percent(step)}%")
                if (showTicks(step, label, firstStep, lastStep)) {
                    div(baseClass = "slider".component("step", "tick")) {}
                }
                val showStep = if (firstStep || lastStep)
                    showBoundaries(step, label, firstStep, lastStep)
                else
                    showSteps(step, label, firstStep, lastStep)
                if (showStep) {
                    div(baseClass = "slider".component("step", "label")) {
                        +label
                        inlineStyle("cursor:pointer")
                        clicks.map { step } handledBy value.update
                    }
                }
            }
        }
    }

    private fun coerceIn(value: Int): Int = if (steps.isNotEmpty()) {
        val values = steps.map { it.value }.toIntArray().sorted()
        if (value !in values) {
            closest(values, value)
        } else {
            value
        }
    } else if (!progression.isEmpty()) {
        if (value !in progression.values) {
            closest(progression.values.sorted(), value)
        } else {
            value
        }
    } else {
        value
    }

    private fun closest(sortedValues: List<Int>, value: Int) = if (value <= sortedValues.first()) {
        sortedValues.first()
    } else if (value >= sortedValues.last()) {
        sortedValues.last()
    } else {
        val index = sortedValues.binarySearch(value)
        if (index >= 0) {
            sortedValues[index]
        } else {
            val insertionPoint = -index - 1
            if (sortedValues[insertionPoint] - value < value - sortedValues[insertionPoint - 1]) {
                sortedValues[insertionPoint]
            } else {
                sortedValues[insertionPoint - 1]
            }
        }
    }

    private fun percent(current: Int): Double {
        val diff = abs(boundary.first)
        val percent = (current + diff).toDouble() / (boundary.last + diff).toDouble() * 100
        return (percent * 100.0).roundToInt() / 100.0 // round to two decimal places
    }
}

public typealias StepPredicate = (step: Int, label: String, first: Boolean, last: Boolean) -> Boolean

internal val SHOW_PREDICATE: StepPredicate = { _, _, _, _ -> true }

internal val LABEL_PREDICATE: StepPredicate = { _, label, _, _ -> label.isNotBlank() }

internal val HIDE_PREDICATE: StepPredicate = { _, _, _, _ -> false }

// ------------------------------------------------------ step

public infix fun StepProgression.step(step: Int): StepProgression {
    require(step > 0) { "Step must be positive, was $step" }
    return copy(step = step)
}

public data class StepProgression(val first: Step, val last: Step, val step: Int = 1) {

    public fun isEmpty(): Boolean =
        if (step > 0) first.value > last.value else first.value < last.value

    // We don't need a Step class for each step, just start, end and an int progression in between
    internal val values: IntProgression = first.value..last.value step step

    internal companion object {
        val EMPTY: StepProgression = StepProgression(Step(1), Step(0), 1)
    }
}

public data class Step(val value: Int, val label: String = "") {

    public operator fun rangeTo(endInclusive: Step): StepProgression = StepProgression(this, endInclusive)
}
