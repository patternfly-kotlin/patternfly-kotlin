package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Select
import dev.fritz2.dom.html.TextArea

// ------------------------------------------------------ dsl

public fun HtmlElements.pfInputFormControl(
    id: String? = null,
    baseClass: String? = null,
    content: Input.() -> Unit = {}
): Input = register(Input(id = id, baseClass = classes("form-control".component(), baseClass)), content)

public fun HtmlElements.pfSelectFormControl(
    id: String? = null,
    baseClass: String? = null,
    content: Select.() -> Unit = {}
): Select = register(Select(id = id, baseClass = classes("form-control".component(), baseClass)), content)

public fun HtmlElements.pfTextareaFormControl(
    id: String? = null,
    baseClass: String? = null,
    content: TextArea.() -> Unit = {}
): TextArea = register(TextArea(id = id, baseClass = classes("form-control".component(), baseClass)), content)
