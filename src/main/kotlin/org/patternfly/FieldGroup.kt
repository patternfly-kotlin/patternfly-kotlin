package org.patternfly

import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import org.patternfly.dom.Id
import org.patternfly.dom.aria
import org.patternfly.dom.hideIf

// ------------------------------------------------------ dsl

/**
 * Creates a [FieldGroup] component inside a [Form] component.
 *
 * @param expandable whether the field group is expandable
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun RenderContext.fieldGroup(
    expandable: Boolean,
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroup.() -> Unit = {}
): FieldGroup = register(FieldGroup(expandable, id = id, baseClass = baseClass, job = job, content = content), {})

/**
 * Creates a [FieldGroupHeader] component inside a [FieldGroup] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroup.fieldGroupHeader(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupHeader.() -> Unit = {}
): FieldGroupHeader = register(
    FieldGroupHeader(
        this,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

/**
 * Creates a [FieldGroupHeaderMain] component inside a [FieldGroupHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroupHeader.fieldGroupHeaderMain(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupHeaderMain.() -> Unit = {}
): FieldGroupHeaderMain = register(
    FieldGroupHeaderMain(
        this.fieldGroup,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

/**
 * Creates a [FieldGroupHeaderTitle] component inside a [FieldGroupHeaderMain] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroupHeaderMain.fieldGroupHeaderTitle(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupHeaderTitle.() -> Unit = {}
): FieldGroupHeaderTitle = register(
    FieldGroupHeaderTitle(
        this.fieldGroup,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

/**
 * Creates a [FieldGroupHeaderTitleText] component inside a [FieldGroupHeaderTitle] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroupHeaderTitle.fieldGroupHeaderTitleText(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupHeaderTitleText.() -> Unit = {}
): FieldGroupHeaderTitleText = register(
    FieldGroupHeaderTitleText(
        this.fieldGroup,
        id = id,
        baseClass = baseClass,
        job
    ),
    content
)

/**
 * Creates a [FieldGroupHeaderDescription] component inside a [FieldGroupHeaderMain] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroupHeaderMain.fieldGroupHeaderDescription(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupHeaderDescription.() -> Unit = {}
): FieldGroupHeaderDescription = register(FieldGroupHeaderDescription(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [FieldGroupHeaderActions] component inside a [FieldGroupHeader] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroupHeader.fieldGroupHeaderActions(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupHeaderActions.() -> Unit = {}
): FieldGroupHeaderActions = register(FieldGroupHeaderActions(id = id, baseClass = baseClass, job), content)

/**
 * Creates a [FieldGroupBody] component inside a [FieldGroup] component.
 *
 * @param id the ID of the element
 * @param baseClass optional CSS class that should be applied to the element
 * @param content a lambda expression for setting up the component itself
 */
public fun FieldGroup.fieldGroupBody(
    id: String? = null,
    baseClass: String? = null,
    content: FieldGroupBody.() -> Unit = {}
): FieldGroupBody = register(FieldGroupBody(this, id = id, baseClass = baseClass, job), content)

// ------------------------------------------------------ tag

/**
 * A field group is a (expandable) container which can contain [FormControl]s or nested [FieldGroup]s. A field group contains an header with additional elements such as a title, description and actions.
 *
 * ```
 * ┏━━━━━━━━━━━━━━━━━━━━━━ fieldGroup: FieldGroup ━━━━━━━━━━━━━━━━━━━━━━┓
 * ┃                                                                    ┃
 * ┃ ┌────────────── fieldGroupHeader: FieldGroupHeader ──────────────┐ ┃
 * ┃ │                                                                │ ┃
 * ┃ │ ┌──────── fieldGroupHeaderMain: FieldGroupHeaderMain ────────┐ │ ┃
 * ┃ │ │                                                            │ │ ┃
 * ┃ │ │ ┌───── fieldGroupHeaderTitle: FieldGroupHeaderTitle ─────┐ │ │ ┃
 * ┃ │ │ │ ┌────────────────────────────────────────────────────┐ │ │ │ ┃
 * ┃ │ │ │ │fieldGroupHeaderTitleText: FieldGroupHeaderTitleText│ │ │ │ ┃
 * ┃ │ │ │ └────────────────────────────────────────────────────┘ │ │ │ ┃
 * ┃ │ │ └────────────────────────────────────────────────────────┘ │ │ ┃
 * ┃ │ │ ┌────────────────────────────────────────────────────────┐ │ │ ┃
 * ┃ │ │ │fieldGroupHeaderDescription: FieldGroupHeaderDescription│ │ │ ┃
 * ┃ │ │ └────────────────────────────────────────────────────────┘ │ │ ┃
 * ┃ │ └────────────────────────────────────────────────────────────┘ │ ┃
 * ┃ │ ┌────────────────────────────────────────────────────────────┐ │ ┃
 * ┃ │ │      fieldGroupHeaderActions: FieldGroupHeaderActions      │ │ ┃
 * ┃ │ └────────────────────────────────────────────────────────────┘ │ ┃
 * ┃ └────────────────────────────────────────────────────────────────┘ ┃
 * ┃                                                                    ┃
 * ┃ ┌──────────────── fieldGroupBody: FieldGroupBody ────────────────┐ ┃
 * ┃ │ ┌────────────────────────────────────────────────────────────┐ │ ┃
 * ┃ │ │                  formControl: FormControl                  │ │ ┃
 * ┃ │ └────────────────────────────────────────────────────────────┘ │ ┃
 * ┃ │ ┌────────────────────────────────────────────────────────────┐ │ ┃
 * ┃ │ │                   fieldGroup: FieldGroup                   │ │ ┃
 * ┃ │ └────────────────────────────────────────────────────────────┘ │ ┃
 * ┃ └────────────────────────────────────────────────────────────────┘ ┃
 * ┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
 * ```
 */
public class FieldGroup internal constructor(
    internal val expandable: Boolean,
    id: String?,
    baseClass: String?,
    job: Job,
    content: FieldGroup.() -> Unit
) : Div(
    id = id,
    baseClass = classes {
        +"form".component("field-group")
        +("expandable".modifier() `when` expandable)
        +baseClass
    },
    job
) {

    private val toggleId = Id.unique("fg", "tgl")
    internal val titleId = Id.unique("fg", "ttl")

    /**
     * Manages the expanded state of this field group.
     */
    public val expanded: ExpandedStore = ExpandedStore()

    init {
        if (expandable) {
            classMap(expanded.data.map { expanded -> mapOf("expanded".modifier() to expanded) })
            div(baseClass = "form".component("field-group", "toggle")) {
                div(baseClass = "form".component("field-group", "toggle", "button")) {
                    pushButton(ButtonVariation.plain, id = this@FieldGroup.toggleId) {
                        aria["labelledby"] = this@FieldGroup.titleId
                        span(baseClass = "form".component("field-group", "toggle", "icon")) {
                            icon("angle-right".fas())
                        }
                    }
                }
            }
        }
        content(this)
    }
}

public class FieldGroupHeader internal constructor(
    internal val fieldGroup: FieldGroup,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id,
    baseClass = classes("form".component("field-group", "header"), baseClass),
    job
)

public class FieldGroupHeaderMain internal constructor(
    internal val fieldGroup: FieldGroup,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id,
    baseClass = classes("form".component("field-group", "header", "main"), baseClass),
    job
)

public class FieldGroupHeaderTitle internal constructor(
    internal val fieldGroup: FieldGroup,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id,
    baseClass = classes("form".component("field-group", "header", "title"), baseClass),
    job
)

public class FieldGroupHeaderTitleText internal constructor(
    fieldGroup: FieldGroup,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id ?: fieldGroup.titleId,
    baseClass = classes("form".component("field-group", "header", "title", "text"), baseClass),
    job
)

public class FieldGroupHeaderDescription internal constructor(
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id,
    baseClass = classes("form".component("field-group", "header", "description"), baseClass),
    job
)

public class FieldGroupHeaderActions internal constructor(
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id,
    baseClass = classes("form".component("field-group", "header", "actions"), baseClass),
    job
)

public class FieldGroupBody internal constructor(
    fieldGroup: FieldGroup,
    id: String?,
    baseClass: String?,
    job: Job
) : Div(
    id = id,
    baseClass = classes("form".component("field-group", "body"), baseClass),
    job
) {
    init {
        if (fieldGroup.expandable) {
            hideIf(fieldGroup.expanded.collapsed)
        }
    }
}
