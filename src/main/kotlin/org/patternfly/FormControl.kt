package org.patternfly

import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Select
import dev.fritz2.dom.html.TextArea

// TODO Document me
// ------------------------------------------------------ dsl

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
