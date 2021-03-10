package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Select
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.TextArea
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import org.patternfly.dom.Id
import org.patternfly.dom.showIf
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLSpanElement

// ------------------------------------------------------ dsl

/**
 * Creates a [FormControl] component inside a [FieldGroupBody] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.formControl(
    id: String? = null,
    baseClass: String? = null,
    content: FormControl.() -> Unit = {}
): FormControl = register(FormControl(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [FormControlLabel] component inside a [FormControl] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FormControl.formControlLabel(
    id: String? = null,
    baseClass: String? = null,
    content: FormControlLabel.() -> Unit = {}
): FormControlLabel = register(FormControlLabel(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [FormControlControl] component inside a [FormControl] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FormControl.formControlControl(
    id: String? = null,
    baseClass: String? = null,
    content: FormControlControl.() -> Unit = {}
): FormControlControl = register(FormControlControl(this, id = id, baseClass = baseClass, job), content)

public fun RenderContext.inputFormControl(
    id: String? = null,
    baseClass: String? = null,
    content: Input.() -> Unit = {}
): Input = register(Input(id = id, baseClass = classes("form-control".component(), baseClass), job), content)

public fun RenderContext.selectFormControl(
    id: String? = null,
    baseClass: String? = null,
    content: Select.() -> Unit = {}
): Select = register(Select(id = id, baseClass = classes("form-control".component(), baseClass), job), content)

public fun RenderContext.textareaFormControl(
    id: String? = null,
    baseClass: String? = null,
    content: TextArea.() -> Unit = {}
): TextArea = register(TextArea(id = id, baseClass = classes("form-control".component(), baseClass), job), content)

// ------------------------------------------------------ tag

/**
 * A form control represents a single input element inside a [Form]. It must be created with an unique identifier and name. If the name is omitted, it's derived from the identifier. A form control can be marked required. Required form controls are indicated visually with an asterisk (*) and the related input element is marked as required (if supported by the HTML element).
 *
 * A form control consists of the following basic elements:
 *
 * 1. Label (optional)
 * 1. Helper text (optional)
 * 1. Popover (optional)
 * 1. Input field
 * 1. Validation message (optional)
 *
 * ### Label
 *
 * You should always provide labels for text and data input so that users understand what information is being requested of them. Labels can be aligned at the top or to the left of the input field (depends on how the surrounding form has been created).
 *
 * ### Helper text
 *
 * An helper text is a permanent text below the input field that helps a user provide the right information, like "Enter a unique name".
 *
 * ### Popover
 *
 * Popovers are content boxes that are used for input fields that might require additional background or explanation. You can also use a popover to link to external help pages or other related information. In forms, popovers are indicated by an unfilled question mark circle that a user can click on to reveal the information.
 *
 * ### Input field
 *
 * PatternFly fritz2 supports a wide range of input fields. Each input field has to be created with a specific store which is bound to a specific type. The following tables shows the supported input fields and the related type:
 *
 *
 * ### Errors and validation
 */
public class FormControl internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("form".component("group"), baseClass), job) {

    internal val groupId: String = Id.unique("frm", "grp")
}

public class FormControlLabel internal constructor(
    id: String?,
    baseClass: String?,
    job: Job
) : WithTextDelegate<HTMLDivElement, HTMLSpanElement>,
    Div(id = id, baseClass = classes("form".component("group", "label"), baseClass), job) {

    private val label: Label
    private lateinit var text: Span
    private lateinit var required: Span

    init {
        label = label(baseClass = "form".component("label")) {
            this@FormControlLabel.text = span(baseClass = "form".component("label", "text")) {}
            this@FormControlLabel.required = span(baseClass = "form".component("label", "required")) {
                domNode.innerHTML = "&#42;"
            }
        }
        required(false)
        // TODO Popover help
    }

    override fun delegate(): HTMLSpanElement = text.domNode

    public fun `for`(value: String) {
        label.attr("for", value)
    }

    public fun `for`(value: Flow<String>) {
        label.attr("for", value)
    }

    public fun required(value: Boolean) {
        required.showIf(value)
    }

    public fun required(value: Flow<Boolean>) {
        required.showIf(value)
    }
}

public class FormControlControl internal constructor(
    formControl: FormControl,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(id = id, baseClass = classes("form".component("group", "control"), baseClass), job)
