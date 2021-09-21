package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TextElement
import kotlinx.coroutines.Job

// ------------------------------------------------------ dsl

/**
 * Creates a [FormSection] component inside a [Form] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.formSection(
    id: String? = null,
    baseClass: String? = null,
    content: FormSection.() -> Unit = {}
): FormSection = register(FormSection(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [FormSectionTitle] component inside a [FormSection] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FormSection.formSectionTitle(
    id: String? = null,
    baseClass: String? = null,
    content: FormSectionTitle.() -> Unit = {}
): FormSectionTitle = register(FormSectionTitle(id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * A Form section is used to group several [FormControl]s or [FieldGroup]s with an optional title.
 *
 * ```
 * ┏━━━━━━━━ formSection: FormSection ━━━━━━━━┓
 * ┃                                          ┃
 * ┃ ┌──────────────────────────────────────┐ ┃
 * ┃ │  formSectionTitle: FormSectionTitle  │ ┃
 * ┃ └──────────────────────────────────────┘ ┃
 * ┃ ┌──────────────────────────────────────┐ ┃
 * ┃ │       formControl: FormControl       │ ┃
 * ┃ └──────────────────────────────────────┘ ┃
 * ┃ ┌──────────────────────────────────────┐ ┃
 * ┃ │        fieldGroup: FieldGroup        │ ┃
 * ┃ └──────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 */
public class FormSection internal constructor(id: String?, baseClass: String?, job: Job) :
    TextElement("section", id = id, baseClass = classes("form".component("section"), baseClass), job, Scope())

public class FormSectionTitle internal constructor(id: String?, baseClass: String?, job: Job) :
    Div(id = id, baseClass = classes("form".component("section", "title"), baseClass), job, Scope())
