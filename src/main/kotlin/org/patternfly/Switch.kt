package org.patternfly

import dev.fritz2.binding.const
import dev.fritz2.dom.DomMountPoint
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.Span
import dev.fritz2.elemento.Id
import dev.fritz2.elemento.aria
import dev.fritz2.elemento.plusAssign
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.w3c.dom.HTMLLabelElement

// ------------------------------------------------------ dsl

public fun RenderContext.pfSwitch(
    id: String? = null,
    baseClass: String? = null,
    content: Switch.() -> Unit = {}
): Switch = register(Switch(id = id, baseClass = baseClass), content)

// ------------------------------------------------------ tag

public class Switch internal constructor(id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLLabelElement>, Label(id = id, baseClass = classes(ComponentType.Switch, baseClass)) {

    public var label: Flow<String>
        get() = with(labelTag.domNode.textContent) {
            if (this == null) emptyFlow() else flowOf(this)
        }
        set(value) {
            toggleTag.domNode.clear()
            DomMountPoint(value.map { TextNode(it) }.distinctUntilChanged(), labelTag.domNode)
        }

    public var labelOff: Flow<String>
        get() = with(labelOffTag.domNode.textContent) {
            if (this == null) emptyFlow() else flowOf(this)
        }
        set(value) {
            toggleTag.domNode.clear()
            DomMountPoint(value.map { TextNode(it) }.distinctUntilChanged(), labelOffTag.domNode)
        }

    public var disabled: Flow<Boolean>
        get() = input.disabled
        set(value) {
            input.disabled = value
        }

    public val input: Input
    private val toggleTag: Span
    private val labelTag: Span
    private val labelOffTag: Span

    init {
        markAs(ComponentType.Switch)
        val inputId = Id.unique(ComponentType.Switch.id, "chk")
        val onId = Id.unique(ComponentType.Switch.id, "on")
        val offId = Id.unique(ComponentType.Switch.id, "off")
        domNode.htmlFor = inputId
        input = input(id = inputId, baseClass = "switch".component("input")) {
            type = const("checkbox")
            aria["labelledby"] = onId
        }
        toggleTag = span(baseClass = "switch".component("toggle")) {
            span(baseClass = "switch".component("toggle", "icon")) {
                icon("check".fas())
            }
        }
        labelTag = span(id = onId, baseClass = "switch".component("label")) {
            domNode.classList += "on".modifier()
            aria["hidden"] = true
        }
        labelOffTag = span(id = offId, baseClass = "switch".component("label")) {
            domNode.classList += "off".modifier()
            aria["hidden"] = true
        }
    }
}
