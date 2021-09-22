package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import kotlinx.coroutines.Job
import org.patternfly.Orientation.HORIZONTAL
import org.w3c.dom.HTMLFormElement

// ------------------------------------------------------ dsl

/**
 * Creates a [Form] component.
 *
 * @param labelOrientation controls the label orientation
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.form(
    labelOrientation: Orientation,
    id: String? = null,
    baseClass: String? = null,
    content: Form.() -> Unit = {}
): Form = register(Form(labelOrientation = labelOrientation, id = id, baseClass = baseClass, job), content)

/**
 * Creates a [FormActions] component inside a [Form] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the actions
 */
public fun Form.formActions(
    id: String? = null,
    baseClass: String? = null,
    content: Div.() -> Unit = {}
): FormActions = register(FormActions(id = id, baseClass = baseClass, job, content), {})

// ------------------------------------------------------ tag

/**
 * PatternFly [form](https://www.patternfly.org/v4/components/form/design-guidelines) component.
 *
 * A form is a group of elements used to collect information from a user.
 *
 * A from can contain multiple [FormSection]s, [FieldGroup]s, [FormControl]s and a single [FormActions] component:
 *
 * ```
 * ┏━━━━━━━━━━ form: Form ━━━━━━━━━━┓
 * ┃                                ┃
 * ┃ ┌────────────────────────────┐ ┃
 * ┃ │  formSection: FormSection  │ ┃
 * ┃ └────────────────────────────┘ ┃
 * ┃ ┌────────────────────────────┐ ┃
 * ┃ │   fieldGroup: FieldGroup   │ ┃
 * ┃ └────────────────────────────┘ ┃
 * ┃ ┌────────────────────────────┐ ┃
 * ┃ │  formControl: FormControl  │ ┃
 * ┃ └────────────────────────────┘ ┃
 * ┃ ┌────────────────────────────┐ ┃
 * ┃ │  formActions: FormActions  │ ┃
 * ┃ └────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 *
 * @sample org.patternfly.sample.FormSample.form
 */
public class Form internal constructor(labelOrientation: Orientation, id: String?, baseClass: String?, job: Job) :
    PatternFlyElement<HTMLFormElement>,
    dev.fritz2.dom.html.Form(
        id = id,
        baseClass = classes {
            +ComponentType.Form
            +("horizontal".modifier() `when` (labelOrientation == HORIZONTAL))
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        markAs(ComponentType.Form)
    }
}

/**
 * Form actions component which holds buttons related to a [Form].
 */
public class FormActions internal constructor(id: String?, baseClass: String?, job: Job, content: Div.() -> Unit) :
    Div(
        id = id,
        baseClass = classes {
            +"form".component("group")
            +"action".modifier()
            +baseClass
        },
        job,
        scope = Scope()
    ) {

    init {
        div(baseClass = "form".component("actions"), content = content)
    }
}
