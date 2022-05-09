package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.shortcutOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.patternfly.ButtonVariant.plain
import org.w3c.dom.HTMLInputElement
import kotlin.math.abs
import kotlin.math.roundToInt

// ------------------------------------------------------ factory

/**
 * Creates a continuous slider using an int progression.
 * Boundaries are shown, steps and ticks are hidden by default.
 *
 * @param value the initial value
 * @param steps the possible values
 * @param disabled whether the slider is disabled
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.SliderSample.intProgression
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    steps: IntProgression,
    disabled: Store<Boolean> = storeOf(false),
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value = value,
    steps = emptyList(),
    progression = Step(steps.first, steps.first.toString())..Step(steps.last, steps.last.toString()) step steps.step,
    showBoundaries = SHOW_PREDICATE,
    showSteps = HIDE_PREDICATE,
    showTicks = HIDE_PREDICATE,
    disabled = disabled
).apply(context).render(this, baseClass, id)

/**
 * Creates a continuous slider using a step progression.
 * The step progression can have custom labels for the boundaries and uses an int progression for the steps in between.
 * Boundaries are shown, steps and ticks are hidden by default.
 *
 * @param value the initial value
 * @param steps the possible values
 * @param disabled whether the slider is disabled
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.SliderSample.stepProgression
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    steps: StepProgression,
    disabled: Store<Boolean> = storeOf(false),
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value = value,
    steps = emptyList(),
    progression = steps,
    showBoundaries = SHOW_PREDICATE,
    showSteps = HIDE_PREDICATE,
    showTicks = HIDE_PREDICATE,
    disabled = disabled
).apply(context).render(this, baseClass, id)

/**
 * Creates a slider using custom steps.
 * Boundaries and steps are shown as defined by the custom steps, ticks are shown by default.
 *
 * @param value the initial value
 * @param steps the possible values
 * @param disabled whether the slider is disabled
 * @param baseClass optional CSS class that should be applied to the element
 * @param id the ID of the element
 * @param context a lambda expression for setting up the component itself
 *
 * @sample org.patternfly.sample.SliderSample.customSteps
 */
public fun RenderContext.slider(
    value: Store<Int> = storeOf(0),
    steps: List<Step>,
    disabled: Store<Boolean> = storeOf(false),
    baseClass: String? = null,
    id: String? = null,
    context: Slider.() -> Unit = {}
): Slider = Slider(
    value = value,
    steps = steps,
    progression = StepProgression.EMPTY,
    showBoundaries = SHOW_PREDICATE,
    showSteps = SHOW_PREDICATE,
    showTicks = SHOW_PREDICATE,
    disabled = disabled
).apply(context).render(this, baseClass, id)

// ------------------------------------------------------ component

/**
 * PatternFly [slider](https://www.patternfly.org/v4/components/slider/design-guidelines) component.
 *
 * A slider provides a quick and effective way for users to set and adjust a numeric value from a defined range of values.
 */
@Suppress("TooManyFunctions")
public open class Slider(
    private var value: Store<Int>,
    private val steps: List<Step>,
    private val progression: StepProgression,
    private var showBoundaries: StepPredicate,
    private var showSteps: StepPredicate,
    private var showTicks: StepPredicate,
    private var disabled: Store<Boolean>,
) : PatternFlyComponent<Slider>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val boundary: IntRange = if (steps.isNotEmpty())
        steps.first().value..steps.last().value
    else if (!progression.isEmpty())
        progression.values.first..progression.values.last
    else
        IntRange.EMPTY

    private var floatingValueInput: Boolean = false
    private var valueInput: SubComponent<Input>? = null
    private var valueLabel: ((Int) -> String)? = null
    private var valueWidth: Store<Int> = storeOf(1)

    private val headActions: SliderActions = SliderActions()
    private val tailActions: SliderActions = SliderActions()

    init {
        // make sure initial value is valid
        val validValue = coerceIn(value.current)
        value.update(validValue)
        valueWidth.update(validValue.toString().length)
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
    public fun disable(value: Boolean) {
        disabled.update(value)
    }

    public fun valueInput(
        floating: Boolean = false,
        baseClass: String? = null,
        id: String? = null,
        context: Input.() -> Unit = {}
    ) {
        this.floatingValueInput = floating
        this.valueInput = SubComponent(baseClass, id, context)
    }

    public fun valueLabel(label: String) {
        valueLabel { _ -> label }
    }

    public fun valueLabel(label: (Int) -> String) {
        this.valueLabel = label
    }

    public fun headAction(context: SliderActions.() -> Unit) {
        context(headActions)
    }

    public fun tailAction(context: SliderActions.() -> Unit) {
        context(tailActions)
    }

    @Suppress("ComplexMethod", "LongMethod")
    override fun render(context: RenderContext, baseClass: String?, id: String?): Slider = with(context) {
        div(baseClass = classes(ComponentType.Slider, baseClass), id = id) {
            markAs(ComponentType.Slider)
            applyElement(this)
            applyEvents(this)
            classMap(disabled.data.map { mapOf("disabled".modifier() to it) })
            val style = if (valueInput != null) {
                value.data.combine(valueWidth.data) { value, width -> value to width }.map { (value, width) ->
                    "--pf-c-slider--value: ${percent(value)}%; --pf-c-slider__value--c-form-control--width-chars:$width"
                }
            } else {
                value.data.map { "--pf-c-slider--value: ${percent(it)}%" }
            }
            inlineStyle(style)

            renderActions(this, headActions)
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
                    if (steps.isNotEmpty()) {
                        aria["valuemin"] = steps.first().value
                        aria["valuemin"] = steps.last().value
                    } else if (!progression.isEmpty()) {
                        aria["valuemin"] = progression.first
                        aria["valuemin"] = progression.last
                    }
                    attr("aria-valuenow", value)
                    attr("role", "slider")
                    attr("tabindex", disabled.data.map { if (it) -1 else 0 })
                }

                if (floatingValueInput) {
                    valueInput?.let { vi ->
                        div(baseClass = classes("slider".component("value"), "floating".modifier())) {
                            renderValueInput(this, vi)
                        }
                    }
                }
            }

            if (!floatingValueInput) {
                valueInput?.let { vi ->
                    div(baseClass = "slider".component("value")) {
                        renderValueInput(this, vi)
                    }
                }
            }
            renderActions(this, tailActions)
        }
        this@Slider
    }

    private fun renderStep(context: RenderContext, step: Int, label: String, firstStep: Boolean, lastStep: Boolean) {
        with(context) {
            div(baseClass = "slider".component("step")) {
                classMap(value.data.map { mapOf("active".modifier() to (it >= step)) })
                inlineStyle("--pf-c-slider__step--Left: ${percent(step)}%")
                if (showTicks(step)) {
                    div(baseClass = "slider".component("step", "tick")) {}
                }
                if (label.isNotBlank()) {
                    val showStep = if (firstStep || lastStep) showBoundaries(step) else showSteps(step)
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
    }

    private fun renderValueInput(context: RenderContext, vi: SubComponent<Input>) {
        with(context) {
            if (valueLabel != null) {
                valueLabel?.let { vl ->
                    // TODO Replace with input group component
                    div(baseClass = "input-group".component()) {
                        renderInputElement(this, vi)
                        span(baseClass = classes("input-group".component("text"), "plain".modifier())) {
                            value.data.map { vl(it) }.renderText(into = this)
                        }
                    }
                }
            } else {
                renderInputElement(this, vi)
            }
        }
    }

    private fun renderInputElement(context: RenderContext, vi: SubComponent<Input>) {
        with(context) {
            input(
                baseClass = classes("form-control".component(), vi.baseClass),
                id = vi.id
            ) {
                type("number")
                aria["label"] = "Slider value input"
                disabled(disabled.data)

                // update value input with value store
                value(value.data.map { it.toString() })

                // update value width on any keystroke...
                keyups.map {
                    it.target.unsafeCast<HTMLInputElement>().value.ifEmpty { " " }.length
                } handledBy valueWidth.update
                // ...and when the value has been changed externally
                value.data.map {
                    it.toString().length
                } handledBy valueWidth.update

                // update value on 'Enter'
                keyups.events.filter { shortcutOf(it) == Keys.Enter }.map {
                    it.preventDefault()
                    it.target.unsafeCast<HTMLInputElement>().value.toIntOrNull()
                }.filterNotNull().map {
                    coerceIn(it)
                } handledBy value.update

                // update value on blur
                blurs.map {
                    it.target.unsafeCast<HTMLInputElement>().value.toIntOrNull()
                }.filterNotNull().map {
                    coerceIn(it)
                } handledBy value.update

                // ignore changes! changes happen only by pressing 'Enter' and on blur
                changes.events.onEach { it.preventDefault() }
            }
        }
    }

    private fun renderActions(context: RenderContext, actions: SliderActions) {
        if (actions.shouldRender) {
            with(context) {
                div(baseClass = "slider".component("actions")) {
                    actions.decrease?.let { dec ->
                        pushButton(plain, baseClass = dec.baseClass, id = dec.id) {
                            element { aria["label"] = "Minus" }
                            icon(classes("minus".fas(), "fa-fw")!!)
                            clicks.map {
                                this@Slider.coerceIn(this@Slider.value.current - 1)
                            } handledBy this@Slider.value.update
                            dec.context(this)
                        }
                    }
                    actions.increase?.let { inc ->
                        pushButton(plain, baseClass = inc.baseClass, id = inc.id) {
                            element { aria["label"] = "Plus" }
                            icon(classes("plus".fas(), "fa-fw")!!)
                            clicks.map {
                                this@Slider.coerceIn(this@Slider.value.current + 1)
                            } handledBy this@Slider.value.update
                            inc.context(this)
                        }
                    }
                    actions.lock?.let { lck ->
                        pushButton(plain, baseClass = lck.baseClass, id = lck.id) {
                            element {
                                aria["label"] = this@Slider.disabled.data.map { if (it) "Locked" else "Unlocked" }
                            }
                            icon("fa-fw") {
                                element {
                                    className(this@Slider.disabled.data.map { if (it) "lock".fas() else "lock-open".fas() })
                                }
                            }
                            clicks.map { !this@Slider.disabled.current } handledBy this@Slider.disabled.update
                            lck.context(this)
                        }
                    }
                    for (action in actions.actions) {
                        pushButton(plain, baseClass = action.baseClass, id = action.id) {
                            action.context(this)
                        }
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

    @Suppress("MagicNumber")
    private fun percent(current: Int): Double {
        val diff = abs(boundary.first)
        val percent = (current + diff).toDouble() / (boundary.last + diff).toDouble() * 100
        return (percent * 100.0).roundToInt() / 100.0 // round to two decimal places
    }
}

public class SliderActions {

    internal var increase: SubComponent<Button>? = null
    internal var decrease: SubComponent<Button>? = null
    internal var lock: SubComponent<Button>? = null
    internal var disabled: Store<Boolean> = storeOf(false)
    internal val actions: MutableList<SubComponent<Button>> = mutableListOf()

    internal val shouldRender: Boolean
        get() = increase != null || decrease != null || lock != null || actions.isNotEmpty()

    public fun increase(
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        this.increase = SubComponent(baseClass, id, context)
    }

    public fun decrease(
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        this.decrease = SubComponent(baseClass, id, context)
    }

    public fun lock(
        disabled: Store<Boolean>? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        disabled?.let { this.disabled = it }
        this.lock = SubComponent(baseClass, id, context)
    }

    public fun action(
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit
    ) {
        actions.add(SubComponent(baseClass, id, context))
    }
}

// ------------------------------------------------------ step

public typealias StepPredicate = (step: Int) -> Boolean

internal val SHOW_PREDICATE: StepPredicate = { _ -> true }

internal val HIDE_PREDICATE: StepPredicate = { _ -> false }

public infix fun StepProgression.step(step: Int): StepProgression {
    require(step > 0) { "Step must be positive, was $step" }
    return copy(step = step)
}

public data class StepProgression(val first: Step, val last: Step, val step: Int = 1) {

    public fun isEmpty(): Boolean =
        if (step > 0) first.value > last.value else first.value < last.value

    // we don't need a Step class for each step, just start, end and an int progression in between
    internal val values: IntProgression = first.value..last.value step step

    internal companion object {
        val EMPTY: StepProgression = StepProgression(Step(1), Step(0), 1)
    }
}

public data class Step(val value: Int, val label: String = "") {

    public operator fun rangeTo(last: Step): StepProgression = StepProgression(this, last)
}
