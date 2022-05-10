package org.patternfly

import dev.fritz2.binding.Store
import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Window
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.shortcutOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import org.patternfly.ButtonVariant.plain
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import kotlin.math.absoluteValue
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
    showBoundaries = SHOW_VALUE,
    showSteps = HIDE_VALUE,
    showTicks = HIDE_VALUE
).apply(context).render(this, baseClass, id)

/**
 * Creates a continuous slider using a step progression.
 * The step progression can have custom labels for the boundaries and uses an int progression for the steps in between.
 * Boundaries are shown, steps and ticks are hidden by default.
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
    showBoundaries = SHOW_VALUE,
    showSteps = HIDE_VALUE,
    showTicks = HIDE_VALUE
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
    showBoundaries = SHOW_VALUE,
    showSteps = SHOW_VALUE,
    showTicks = SHOW_VALUE
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
    private var showBoundaries: ValuePredicate,
    private var showSteps: ValuePredicate,
    private var showTicks: ValuePredicate
) : PatternFlyComponent<Slider>,
    WithElement by ElementMixin(),
    WithEvents by EventMixin() {

    private val boundary: IntRange = if (steps.isNotEmpty())
        steps.first().value..steps.last().value
    else if (!progression.isEmpty())
        progression.values.first..progression.values.last
    else
        IntRange.EMPTY

    private var disabled: Flow<Boolean> = emptyFlow()
    private val sliderDisabled: Boolean
        get() = root.classList.contains("disabled".modifier())

    private var floatingValueInput: Boolean = false
    private var valueInput: SubComponent<Input>? = null
    private var valueLabel: String? = null
    private var valueWidth: Store<Int> = storeOf(1)

    private val leftActions: SliderActions = SliderActions()
    private val rightActions: SliderActions = SliderActions()

    private lateinit var root: HTMLDivElement
    private lateinit var thumbElement: HTMLDivElement
    private lateinit var railElement: HTMLDivElement
    private val thumbLeft: Store<Int> = storeOf(0)
    private val thumbMoving: Store<Boolean> = storeOf(false)

    init {
        // make sure initial value is valid
        val validValue = coerceIn(value.current)
        value.update(validValue)
        valueWidth.update(validValue.toString().length)
    }

    // ------------------------------------------------------ slider API

    /**
     * Hides the boundaries.
     */
    public fun hideBoundaries() {
        showBoundaries(HIDE_VALUE)
    }

    /**
     * Shows the boundaries.
     */
    public fun showBoundaries() {
        showBoundaries(SHOW_VALUE)
    }

    /**
     * Shows the boundaries if the specified condition is met.
     */
    public fun showBoundaries(predicate: ValuePredicate) {
        this.showBoundaries = predicate
    }

    /**
     * Hides the steps.
     */
    public fun hideSteps() {
        showSteps(HIDE_VALUE)
    }

    /**
     * Shows the steps.
     */
    public fun showSteps() {
        showSteps(SHOW_VALUE)
    }

    /**
     * Shows the steps if the specified condition is met.
     */
    public fun showSteps(predicate: ValuePredicate) {
        this.showSteps = predicate
    }

    /**
     * Hides the ticks.
     */
    public fun hideTicks() {
        showTicks(HIDE_VALUE)
    }

    /**
     * Shows the ticks.
     */
    public fun showTicks() {
        showTicks(SHOW_VALUE)
    }

    /**
     * Shows the ticks if the specified condition is met.
     */
    public fun showTicks(predicate: ValuePredicate) {
        this.showTicks = predicate
    }

    /**
     * Disables / enables the slider according to [value].
     */
    public fun disabled(value: Boolean) {
        disabled = flowOf(value)
    }

    /**
     * Disables / enables the slider according to [value].
     */
    public fun disabled(value: Flow<Boolean>) {
        disabled = value
    }

    /**
     * Shows a value input for the slider.
     *
     * @param floating whether the value input is located over the thumb
     * @param label a unit symbol for the value in the input field
     * @param baseClass optional CSS class that should be applied to the value input
     * @param id the ID of the value input
     * @param context a lambda expression for setting up the value input
     *
     * @sample org.patternfly.sample.SliderSample.valueInput
     */
    public fun valueInput(
        floating: Boolean = false,
        label: String? = null,
        baseClass: String? = null,
        id: String? = null,
        context: Input.() -> Unit = {}
    ) {
        this.floatingValueInput = floating
        this.valueLabel = label
        this.valueInput = SubComponent(baseClass, id, context)
    }

    /**
     * Adds actions to the left side of the slider
     *
     * @sample org.patternfly.sample.SliderSample.actions
     */
    public fun leftActions(context: SliderActions.() -> Unit) {
        context(leftActions)
    }

    /**
     * Adds actions to the right side of the slider
     *
     * @sample org.patternfly.sample.SliderSample.actions
     */
    public fun rightActions(context: SliderActions.() -> Unit) {
        context(rightActions)
    }

    // ------------------------------------------------------ render methods

    override fun render(context: RenderContext, baseClass: String?, id: String?): Slider = with(context) {
        div(baseClass = classes(ComponentType.Slider, baseClass), id = id) {
            root = domNode
            markAs(ComponentType.Slider)
            applyElement(this)
            applyEvents(this)
            classMap(disabled.map { mapOf("disabled".modifier() to it) })
            val style = if (valueInput != null) {
                value.data.combine(valueWidth.data) { value, width -> value to width }.map { (value, width) ->
                    "--pf-c-slider--value: ${percent(value)}%; --pf-c-slider__value--c-form-control--width-chars:$width"
                }
            } else {
                value.data.map { "--pf-c-slider--value: ${percent(it)}%" }
            }
            inlineStyle(style)

            renderActions(this, leftActions)
            div(baseClass = "slider".component("main")) {
                div(baseClass = "slider".component("rail")) {
                    renderRail(this)
                }
                div(baseClass = "slider".component("steps")) {
                    renderSteps(this)
                }
                div(baseClass = "slider".component("thumb")) {
                    renderThumb(this)
                }
                if (floatingValueInput) {
                    valueInput?.let { inputComponent ->
                        div(baseClass = classes("slider".component("value"), "floating".modifier())) {
                            renderValueInput(this, inputComponent)
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
            renderActions(this, rightActions)
        }
        this@Slider
    }

    private fun renderRail(context: Div) {
        with(context) {
            railElement = domNode
            clicks.events.filterNot { sliderDisabled }.map { coerceIn(onRailClick(it)) } handledBy value.update
            div(baseClass = "slider".component("rail-track")) {}
        }
    }

    @Suppress("NestedBlockDepth")
    private fun renderSteps(context: Div) {
        with(context) {
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
                        renderStep(this, progression.first.value, progression.first.label, firstStep, lastStep)
                    } else if (lastStep) {
                        renderStep(this, progression.last.value, progression.last.label, firstStep, lastStep)
                    } else {
                        renderStep(this, step, step.toString(), firstStep, lastStep)
                    }
                }
            }
        }
    }

    private fun renderStep(context: RenderContext, step: Int, label: String, firstStep: Boolean, lastStep: Boolean) {
        with(context) {
            div(baseClass = "slider".component("step")) {
                classMap(value.data.map { mapOf("active".modifier() to (it >= step)) })
                inlineStyle("--pf-c-slider__step--Left: ${percent(step)}%")
                if (showTicks(step)) {
                    div(baseClass = "slider".component("step", "tick")) {
                        clicks.events.filterNot { sliderDisabled }.map { step } handledBy value.update
                    }
                }
                if (label.isNotBlank()) {
                    val showStep = if (firstStep || lastStep) showBoundaries(step) else showSteps(step)
                    if (showStep) {
                        div(baseClass = "slider".component("step", "label")) {
                            +label
                            inlineStyle(disabled.map { if (it) "cursor:not-allowed" else "cursor:pointer" })
                            clicks.events.filterNot { sliderDisabled }.map { step } handledBy value.update
                        }
                    }
                }
            }
        }
    }

    private fun renderThumb(context: Div) {
        with(context) {
            thumbElement = domNode
            if (steps.isNotEmpty()) {
                aria["valuemin"] = steps.first().value
                aria["valuemin"] = steps.last().value
            } else if (!progression.isEmpty()) {
                aria["valuemin"] = progression.first
                aria["valuemin"] = progression.last
            }
            attr("aria-valuenow", value)
            attr("aria-disabled", disabled)
            attr("aria-valuenow", value)
            attr("role", "slider")
            attr("tabindex", disabled.map { if (it) -1 else 0 })

            // store thumb position onMouseDown()
            mousedowns.events.filterNot { sliderDisabled }.map { event ->
                event.stopPropagation()
                event.preventDefault()
                event.clientX - thumbElement.getBoundingClientRect().left.toInt()
            } handledBy thumbLeft.update

            // update thumb moving flag onMouseDown() / onMouseUp()
            mousedowns.events.filterNot { thumbMoving.current }.map { true } handledBy thumbMoving.update
            Window.mouseups.events.filter { thumbMoving.current }.map { false } handledBy thumbMoving.update

            // update value onMouseMove()
            Window.mousemoves.events.filter {
                !sliderDisabled && thumbMoving.current
            }.map {
                coerceIn(onThumbMove(it))
            } handledBy value.update

            // update value onKeyDown(← and →)
            keydowns.events.filterNot { sliderDisabled }.map {
                it to shortcutOf(it)
            }.filter { (_, shortcut) ->
                shortcut == Keys.ArrowLeft || shortcut == Keys.ArrowRight
            }.map {
                onThumbLeftRight(
                    it.first,
                    when (it.second) {
                        Keys.ArrowLeft -> -1
                        Keys.ArrowRight -> 1
                        else -> 0
                    }
                )
            } handledBy value.update
        }
    }

    private fun renderValueInput(context: RenderContext, inputComponent: SubComponent<Input>) {
        with(context) {
            if (valueLabel != null) {
                valueLabel?.let { label ->
                    // TODO Replace with input group component
                    div(baseClass = "input-group".component()) {
                        renderValueInputElement(this, inputComponent)
                        span(baseClass = classes("input-group".component("text"), "plain".modifier())) {
                            +label
                        }
                    }
                }
            } else {
                renderValueInputElement(this, inputComponent)
            }
        }
    }

    private fun renderValueInputElement(context: RenderContext, inputComponent: SubComponent<Input>) {
        with(context) {
            input(
                baseClass = classes("form-control".component(), inputComponent.baseClass),
                id = inputComponent.id
            ) {
                type("number")
                aria["label"] = "Slider value input"
                disabled(disabled)

                // bind input value to value store
                value(value.data.map { it.toString() })

                // update value onKeyUp(*)
                keyups.map {
                    it.target.unsafeCast<HTMLInputElement>().value.ifEmpty { " " }.length
                } handledBy valueWidth.update
                // ...and when the value has been changed externally
                value.data.map { it.toString().length } handledBy valueWidth.update

                // update value onKeyUp(Enter)
                keyups.events.filter { shortcutOf(it) == Keys.Enter }.map {
                    it.preventDefault()
                    it.target.unsafeCast<HTMLInputElement>().value.toIntOrNull()
                }.filterNotNull().map {
                    coerceIn(it)
                } handledBy value.update

                // update value onBlur()
                blurs.map {
                    it.target.unsafeCast<HTMLInputElement>().value.toIntOrNull()
                }.filterNotNull().map {
                    coerceIn(it)
                } handledBy value.update

                // ignore change events!
                // value must only be changed onKeyUp(Enter) and onBlur()
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
                                aria["label"] = actions.disabled.data.map { if (it) "Locked" else "Unlocked" }
                            }
                            icon("fa-fw") {
                                element {
                                    className(
                                        actions.disabled.data.map {
                                            if (it) "lock".fas() else "lock-open".fas()
                                        }
                                    )
                                }
                            }
                            clicks.map { !actions.disabled.current } handledBy actions.disabled.update
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

    // ------------------------------------------------------ event handler

    private fun onRailClick(event: MouseEvent): Int = pixelToValue(event.clientX)

    private fun onThumbLeftRight(event: KeyboardEvent, direction: Int): Int {
        event.preventDefault()
        val values = when {
            steps.isNotEmpty() -> steps.map { it.value }
            !progression.isEmpty() -> progression.values.toList()
            else -> emptyList()
        }
        return if (values.isNotEmpty()) {
            val currentIndex = values.indexOfFirst { it == value.current }
            val newIndex = (currentIndex + direction).coerceIn(0..values.lastIndex)
            values[newIndex]
        } else {
            value.current
        }
    }

    private fun onThumbMove(event: MouseEvent): Int = pixelToValue(event.clientX - thumbLeft.current)

    private fun pixelToValue(x: Int): Int {
        // px -> % -> value
        val left = x - railElement.getBoundingClientRect().left.toInt()
        val width = railElement.offsetWidth - (thumbElement.offsetWidth / 2)
        val position = left.coerceIn(0..width)
        val fraction = (position.toDouble() / width.toDouble())
        return (((boundary.last - boundary.first).absoluteValue) * fraction).roundToInt() + boundary.first
    }

    // ------------------------------------------------------ helper methods

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
        val diff = if (boundary.first < 0) boundary.first.absoluteValue else boundary.first * -1
        val percent = (current + diff).toDouble() / (boundary.last + diff).toDouble() * 100
        return (percent * 100.0).roundToInt() / 100.0 // round to two decimal places
    }
}

/**
 * Class for adding actions to a slider component.
 */
public class SliderActions {

    internal var increase: SubComponent<Button>? = null
    internal var decrease: SubComponent<Button>? = null
    internal var lock: SubComponent<Button>? = null
    internal var disabled: Store<Boolean> = storeOf(false)
    internal val actions: MutableList<SubComponent<Button>> = mutableListOf()

    internal val shouldRender: Boolean
        get() = increase != null || decrease != null || lock != null || actions.isNotEmpty()

    /**
     * Adds an increase button, that increases the slider value.
     */
    public fun increase(
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        this.increase = SubComponent(baseClass, id, context)
    }

    /**
     * Adds an increase button, that decreases the slider value.
     */
    public fun decrease(
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        this.decrease = SubComponent(baseClass, id, context)
    }

    /**
     * Adds a lock button, that updates the specified store. The store should be used for [Slider.disabled]
     */
    public fun lock(
        disabled: Store<Boolean>,
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit = {}
    ) {
        this.disabled = disabled
        this.lock = SubComponent(baseClass, id, context)
    }

    /**
     * Adds an arbitrary action.
     */
    public fun action(
        baseClass: String? = null,
        id: String? = null,
        context: Button.() -> Unit
    ) {
        actions.add(SubComponent(baseClass, id, context))
    }
}

// ------------------------------------------------------ step

public typealias ValuePredicate = (step: Int) -> Boolean

internal val SHOW_VALUE: ValuePredicate = { _ -> true }

internal val HIDE_VALUE: ValuePredicate = { _ -> false }

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
