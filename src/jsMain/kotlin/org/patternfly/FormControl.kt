package org.patternfly

import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Select
import dev.fritz2.dom.html.TextArea

// ------------------------------------------------------ dsl

fun HtmlElements.pfInputFormControl(
    id: String? = null,
    classes: String? = null,
    content: Input.() -> Unit = {}
): Input = register(Input(id = id, baseClass = classes("form-control".component(), classes)), content)

fun HtmlElements.pfSelectFormControl(
    id: String? = null,
    classes: String? = null,
    content: Select.() -> Unit = {}
): Select = register(Select(id = id, baseClass = classes("form-control".component(), classes)), content)

fun HtmlElements.pfTextareaFormControl(
    id: String? = null,
    classes: String? = null,
    content: TextArea.() -> Unit = {}
): TextArea = register(TextArea(id = id, baseClass = classes("form-control".component(), classes)), content)
