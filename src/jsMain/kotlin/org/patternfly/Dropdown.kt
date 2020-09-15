package org.patternfly

import dev.fritz2.binding.OfferingHandler
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SingleMountPoint
import dev.fritz2.binding.action
import dev.fritz2.binding.const
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.DomMountPoint
import dev.fritz2.dom.DomMountPointPreserveOrder
import dev.fritz2.dom.Tag
import dev.fritz2.dom.TextNode
import dev.fritz2.dom.WithDomNode
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.Button
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Input
import dev.fritz2.dom.html.Label
import dev.fritz2.dom.html.Li
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.dom.clear
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.HTMLUListElement
import org.w3c.dom.Node
import org.w3c.dom.Text

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfDropdown(
    store: DropdownStore<T> = DropdownStore(),
    align: Align? = null,
    up: Boolean = false,
    classes: String? = null,
    content: Dropdown<T>.() -> Unit = {}
): Dropdown<T> = register(Dropdown(store, align, up, classes), content)

fun <T> Dropdown<T>.pfDropdownToggle(
    classes: String? = null,
    content: DropdownToggle<T>.() -> Unit = {}
): DropdownToggle<T> = register(DropdownToggle(this, classes), content)

fun <T> Dropdown<T>.pfDropdownToggleKebab(
    classes: String? = null,
    content: DropdownToggle<T>.() -> Unit = {}
): DropdownToggle<T> = register(DropdownToggle(this, classes).apply {
    icon = { pfIcon("ellipsis-v".fas()) }
}, content)

fun <T> Dropdown<T>.pfDropdownToggleCheckbox(
    classes: String? = null,
    content: DropdownToggleCheckbox<T>.() -> Unit = {}
): DropdownToggleCheckbox<T> = register(DropdownToggleCheckbox(this, classes), content)

fun <T> Dropdown<T>.pfDropdownToggleAction(
    classes: String? = null,
    content: DropdownToggleAction<T>.() -> Unit = {}
): DropdownToggleAction<T> = register(DropdownToggleAction(this, classes), content)

fun <T> Dropdown<T>.pfDropdownItems(
    classes: String? = null,
    block: ItemsBuilder<T>.() -> Unit = {}
): DropdownEntries<HTMLUListElement, T> {
    val element = this.register(
        DropdownEntries<HTMLUListElement, T>(this, "ul", classes), {})
    val items = ItemsBuilder<T>().apply(block).build()
    action(items) handledBy this.store.update
    return element
}

fun <T> Dropdown<T>.pfDropdownGroups(
    classes: String? = null,
    block: GroupsBuilder<T>.() -> Unit = {}
): DropdownEntries<HTMLDivElement, T> {
    val element = this.register(
        DropdownEntries<HTMLDivElement, T>(this, "div", classes), {})
    val groups = GroupsBuilder<T>().apply(block).build()
    action(groups) handledBy this.store.update
    return element
}

// ------------------------------------------------------ tag

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class Dropdown<T> internal constructor(
    val store: DropdownStore<T>,
    internal val dropdownAlign: Align?,
    up: Boolean,
    classes: String?
) : PatternFlyComponent<HTMLDivElement>, Div(baseClass = classes {
    +ComponentType.Dropdown
    +dropdownAlign?.modifier
    +("top".modifier() `when` up)
    +classes
}) {

    internal var toggle: DropdownToggleBase<out HTMLElement, T>? = null

    val ces = CollapseExpandStore { target ->
        !domNode.contains(target) && !target.matches(By.classname("dropdown".component("menu-item")))
    }

    var display: ComponentDisplay<Button, Item<T>> = { item ->
        {
            if (item.description.isNotEmpty()) {
                div(baseClass = "dropdown".component("menu-item", "main")) {
                    item.icon?.let { iconDisplay ->
                        span(baseClass = "dropdown".component("menu-item", "icon")) {
                            iconDisplay(this)
                        }
                    }
                    +item.item.toString()
                }
                div(baseClass = "dropdown".component("menu-item", "description")) {
                    +item.description
                }
            } else {
                item.icon?.let { iconDisplay ->
                    span(baseClass = "dropdown".component("menu-item", "icon")) {
                        iconDisplay(this)
                    }
                }
                +item.item.toString()
            }
        }
    }

    init {
        markAs(ComponentType.Dropdown)
        classMap = ces.data.map { expanded -> mapOf("expanded".modifier() to expanded) }
    }
}

sealed class DropdownToggleBase<E : HTMLElement, T>(
    private val dropdown: Dropdown<T>,
    tagName: String,
    id: String? = null,
    baseClass: String? = null,
) : Tag<E>(tagName, id, baseClass), WithText<E> {

    internal var toggleId: String? = null
    abstract var disabled: Flow<Boolean>

    internal fun initToggle(toggleTag: Tag<HTMLElement>) {
        with(toggleTag) {
            aria["haspopup"] = true
            clicks handledBy this@DropdownToggleBase.dropdown.ces.toggle
            this@DropdownToggleBase.toggleId = id
            this@DropdownToggleBase.dropdown.ces.data.map { it.toString() }.bindAttr("aria-expanded")
        }
        dropdown.toggle = this
    }
}

class DropdownToggle<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?,
) : DropdownToggleBase<HTMLButtonElement, T>(
    dropdown = dropdown,
    tagName = "button",
    id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
    baseClass = classes {
        +"dropdown".component("toggle")
        +"plain".modifier()
        +classes
    }),
    WithTextDelegate<HTMLButtonElement, HTMLSpanElement> {

    private var textElement: HTMLSpanElement? = null

    init {
        initToggle(this)
    }

    var icon: (Tag<HTMLButtonElement>.() -> Unit)? = null
        set(value) {
            field = value
            if (textElement != null) {
                textElement.removeFromParent()
                textElement = null
            }
            value?.invoke(this)
        }

    override var disabled: Flow<Boolean>
        get() {
            throw NotImplementedError()
        }
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    domNode.disabled = value
                }
            }
        }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            domNode.clear()
            domNode.classList -= "plain".modifier()
            textElement = register(span(baseClass = "dropdown".component("toggle", "text")) {}, {}).domNode
            register(span(baseClass = "dropdown".component("toggle", "icon")) {
                pfIcon("caret-down".fas())
            }, {})
        }
        return textElement!!
    }
}

enum class TriState { OFF, INDETERMINATE, ON }

class DropdownToggleCheckbox<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?,
) : DropdownToggleBase<HTMLDivElement, T>(
    dropdown = dropdown,
    tagName = "div",
    id = null,
    baseClass = classes {
        +"dropdown".component("toggle")
        +"split-button".modifier()
        +classes
    }),
    WithTextDelegate<HTMLDivElement, HTMLSpanElement> {

    private val labelTag: Label
    private val toggleTag: Button
    private var textElement: HTMLSpanElement? = null
    private val checkId = Id.unique(ComponentType.Dropdown.id, "tgl", "chk")
    val input: Input = Input(checkId)

    init {
        labelTag = label(baseClass = "dropdown".component("toggle", "check")) {
            `for` = const(this@DropdownToggleCheckbox.checkId)
            register(this@DropdownToggleCheckbox.input) {
                it.type = const("checkbox")
            }
        }
        toggleTag = button(
            id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
            baseClass = "dropdown".component("toggle", "button")
        ) {
            this@DropdownToggleCheckbox.initToggle(this)
            pfIcon("caret-down".fas())
        }
    }

    override var disabled: Flow<Boolean>
        get() = throw NotImplementedError()
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    domNode.classList.toggle("disabled".modifier(), value)
                    input.domNode.disabled = value
                    toggleTag.domNode.disabled = value
                }
            }
        }

    var triState: Flow<TriState>
        get() = throw NotImplementedError()
        set(flow) {
            object : SingleMountPoint<TriState>(flow) {
                override fun set(value: TriState, last: TriState?) {
                    when (value) {
                        TriState.OFF -> {
                            input.domNode.checked = false
                            input.domNode.indeterminate = false
                        }
                        TriState.INDETERMINATE -> {
                            input.domNode.checked = false
                            input.domNode.indeterminate = true
                        }
                        TriState.ON -> {
                            input.domNode.checked = true
                            input.domNode.indeterminate = false
                        }
                    }
                }
            }
        }

    override fun Flow<String>.bind(preserveOrder: Boolean): SingleMountPoint<WithDomNode<Text>> {
        val upstream = this.map {
            textElement?.styleHidden = it.isNotEmpty()
            TextNode(it)
        }.distinctUntilChanged()

        return if (preserveOrder) DomMountPointPreserveOrder(upstream, delegate())
        else DomMountPoint(upstream, delegate())
    }

    override fun appendText(text: String): Node {
        val node = super.appendText(text)
        textElement?.styleHidden = text.isNotEmpty()
        return node
    }

    override fun delegate(): HTMLSpanElement {
        if (textElement == null) {
            val textId = Id.unique(ComponentType.Dropdown.id, "tgl", "txt")
            textElement = labelTag.register(
                span(id = textId, baseClass = "dropdown".component("toggle", "text")) {
                    aria["hidden"] = true
                    domNode.style.display = "none"
                }, {}).domNode
            input.aria["labelledby"] = textId
        }
        return textElement!!
    }
}

class DropdownToggleAction<T> internal constructor(
    dropdown: Dropdown<T>,
    classes: String?
) : WithTextDelegate<HTMLDivElement, HTMLButtonElement>,
    DropdownToggleBase<HTMLDivElement, T>(
        dropdown = dropdown,
        tagName = "div",
        id = null,
        baseClass = classes {
            +"dropdown".component("toggle")
            +"split-button".modifier()
            +"action".modifier()
            +classes
        }) {

    val action = button(baseClass = "dropdown".component("toggle", "button")) {}
    private val toggleTag = button(
        id = Id.unique(ComponentType.Dropdown.id, "tgl", "btn"),
        baseClass = "dropdown".component("toggle", "button")
    ) {
        this@DropdownToggleAction.initToggle(this)
        pfIcon("caret-down".fas())
    }

    var icon: (Button.() -> Unit)? = null
        set(value) {
            field = value
            action.domNode.clear()
            value?.invoke(action)
        }

    override var disabled: Flow<Boolean>
        get() = throw NotImplementedError()
        set(flow) {
            object : SingleMountPoint<Boolean>(flow) {
                override fun set(value: Boolean, last: Boolean?) {
                    domNode.classList.toggle("disabled".modifier(), value)
                    action.domNode.disabled = value
                    toggleTag.domNode.disabled = value
                }
            }
        }

    override fun delegate(): HTMLButtonElement = action.domNode
}

class DropdownEntries<E : HTMLElement, T> internal constructor(
    private val dropdown: Dropdown<T>,
    tagName: String,
    classes: String?
) : Tag<E>(tagName = tagName, baseClass = classes {
    +"dropdown".component("menu")
    +dropdown.dropdownAlign?.modifier
    +classes
}) {
    init {
        dropdown.toggle?.toggleId?.let {
            aria["labelledby"] = it
        }
        attr("role", "menu")
        dropdown.ces.data.map { !it }.bindAttr("hidden")

        dropdown.store.data.each().render { entry ->
            when (entry) {
                is Item<T> -> {
                    li(content = itemContent(entry))
                }
                is Group<T> -> {
                    section(baseClass = "dropdown".component("group")) {
                        entry.title?.let {
                            h1(baseClass = "dropdown".component("group", "title")) { +it }
                        }
                        ul {
                            entry.items.forEach { groupEntry ->
                                when (groupEntry) {
                                    is Item<T> -> {
                                        li(content = this@DropdownEntries.itemContent(groupEntry))
                                    }
                                    is Separator<T> -> {
                                        pfDivider(DividerVariant.LI)
                                    }
                                    else -> {
                                        console.warn("Nested groups are not supported for ${this@DropdownEntries.dropdown.domNode.debug()}")
                                    }
                                }
                            }
                        }
                    }
                }
                is Separator<T> -> {
                    if (domNode.tagName.toLowerCase() == "ul") {
                        pfDivider(DividerVariant.LI)
                    } else {
                        pfDivider(DividerVariant.DIV)
                    }
                }
            }
        }.bind()
    }

    private fun itemContent(item: Item<T>): Li.() -> Unit = {
        attr("role", "menuitem")
        button(baseClass = classes {
            +"dropdown".component("menu-item")
            +("icon".modifier() `when` (item.icon != null))
            +("description".modifier() `when` item.description.isNotEmpty())
        }) {
            attr("tabindex", "-1")
            if (item.disabled) {
                aria["disabled"] = true
                attr("disabled", "true")
                domNode.classList += "disabled".modifier()
            }
            clicks.map { item.item } handledBy this@DropdownEntries.dropdown.store.clicked
            clicks handledBy this@DropdownEntries.dropdown.ces.collapse
            if (item.selected) {
                domNode.autofocus = true
            }
            this@DropdownEntries.dropdown.display(item).invoke(this)
        }
    }
}

// ------------------------------------------------------ store

class DropdownStore<T> : RootStore<List<Entry<T>>>(listOf()) {

    internal val clicked: OfferingHandler<T, T> = handleAndOffer { items, item ->
        offer(item)
        items
    }

    val clicks: Flow<T> = clicked.map { it }
    val items: Flow<List<T>> = data.map { it.filterIsInstance<Item<T>>() }.map { it.map { item -> item.item } }
    val groups: Flow<List<Group<T>>> = data.map { it.filterIsInstance<Group<T>>() }
}
