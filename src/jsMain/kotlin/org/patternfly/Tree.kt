package org.patternfly

import dev.fritz2.binding.RootStore
import dev.fritz2.binding.SimpleHandler
import dev.fritz2.binding.action
import dev.fritz2.binding.each
import dev.fritz2.binding.handledBy
import dev.fritz2.dom.html.Div
import dev.fritz2.dom.html.HtmlElements
import dev.fritz2.dom.html.Li
import dev.fritz2.dom.html.Span
import dev.fritz2.dom.html.Ul
import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import org.w3c.dom.HTMLDivElement

// ------------------------------------------------------ dsl

fun <T> HtmlElements.pfTreeView(
    store: TreeStore<T> = TreeStore(),
    id: String? = null,
    baseClass: String? = null,
    content: TreeView<T>.() -> Unit = {}
): TreeView<T> = register(TreeView(store, id = id, baseClass = baseClass), content)

fun <T> TreeView<T>.pfTreeItems(
    block: TreeItemsBuilder<T>.() -> Unit = {}
) {
    val treeItems = TreeItemsBuilder<T>().apply(block).build()
    action(treeItems) handledBy this.store.update
}

fun <T> TreeItemsBuilder<T>.pfTreeItem(
    item: T,
    block: TreeItemBuilder<T>.() -> Unit = {}
) {
    val builder = TreeItemBuilder(item)
    val treeItem = builder.build()
    builder.apply(block)
    treeItems.add(treeItem)
}

fun <T> TreeItemBuilder<T>.pfTreeItems(
    block: TreeItemsBuilder<T>.() -> Unit = {}
) {
    val treeItems = TreeItemsBuilder<T>().apply(block).build()
    treeItems.forEach { current?.add(it) }
}

fun test() {
    render {
        pfTreeView<String> {
            pfTreeItems {
                pfTreeItem("Application Launcher") {
                    pfTreeItems {
                        pfTreeItem("Application 1") {
                            pfTreeItems {
                                pfTreeItem("Settings")
                                pfTreeItem("Current")
                            }
                        }
                        pfTreeItem("Application 2") {
                            pfTreeItems {
                                pfTreeItem("Settings")
                                pfTreeItem("Loader") {
                                    pfTreeItems {
                                        pfTreeItem("Loading App 1")
                                        pfTreeItem("Loading App 2")
                                        pfTreeItem("Loading App 3")
                                    }
                                }
                            }
                        }
                    }
                }
                pfTreeItem("Cost Management") {
                    pfTreeItems {
                        pfTreeItem("Application 3") {
                            pfTreeItems {
                                pfTreeItem("Settings")
                                pfTreeItem("Current")
                            }
                        }
                    }
                }
                pfTreeItem("Sources") {
                    pfTreeItem("Application 4")
                }
                pfTreeItem("Really long folder name that overflows the container it is in") {
                    pfTreeItem("Application 5")
                }
            }
        }
    }
}

// ------------------------------------------------------ tag

class TreeView<T> internal constructor(internal val store: TreeStore<T>, id: String?, baseClass: String?) :
    PatternFlyComponent<HTMLDivElement>, Div(id = id, baseClass = classes(ComponentType.TreeView, baseClass)) {

    init {
        markAs(ComponentType.TreeView)
        ul {
            attr("role", "tree")
            this@TreeView.store.data.each { this@TreeView.store.identifier(it.item) }.render { treeItem ->
                this@TreeView.renderTreeItem(this@ul, treeItem)
            }.bind()
        }
    }

    private fun renderTreeItem(ul: Ul, treeItem: TreeItem<T>): Li {
        val li: Li
        with(ul) {
            li = li(baseClass = classes {
                +"tree-view".component("list-item")
                +("expandable".modifier() `when` treeItem.hasChildren)
                +("expanded".modifier() `when` (treeItem.hasChildren && treeItem.expanded))
            }) {
                attr("role", "treeitem")
                attr("tabindex", "0")
                if (treeItem.hasChildren) {
                    aria["expanded"] = treeItem.expanded
                }
                div(baseClass = "tree-view".component("content")) {
                    button(baseClass = "tree-view".component("node")) {
                        if (treeItem.hasChildren) {
                            span(baseClass = "tree-view".component("node", "toggle", "icon")) {
                                pfIcon("angle-right".fas())
                            }
                        }
                        span(baseClass = "tree-view".component("node", "text")) {
                            +treeItem.item.toString()
                        }
                    }
                }
                if (treeItem.children.isNotEmpty()) {
                    ul {
                        attr("role", "group")
                        for (childItem in treeItem.children) {
                            this@TreeView.renderTreeItem(this@ul, childItem)
                        }
                    }
                }
            }
        }
        return li
    }
}

// ------------------------------------------------------ store

class TreeStore<T>(val identifier: IdProvider<T, String> = { Id.asId(it.toString()) }) :
    RootStore<List<TreeItem<T>>>(listOf()) {

    val select: SimpleHandler<TreeItem<T>> = handle { tree, item ->
        tree
    }
    val toggle: SimpleHandler<TreeItem<T>> = handle { tree, item ->
        tree
    }
}

// ------------------------------------------------------ types

class TreeItemsBuilder<T> {
    internal val treeItems: MutableList<TreeItem<T>> = mutableListOf()
    internal fun build(): List<TreeItem<T>> = treeItems
}

class TreeItemBuilder<T>(private val item: T) {
    var expanded: Boolean = true
    var selected: Boolean = false
    var icon: (Span.() -> Unit)? = null
    var expandedIcon: (Span.() -> Unit)? = null
    internal var current: TreeItem<T>? = null

    internal fun build(): TreeItem<T> {
        current = TreeItem(item, expanded, selected, TriState.OFF, icon, expandedIcon)
        return current!!
    }
}

class TreeItem<T>(
    override val item: T,
    val expanded: Boolean,
    val selected: Boolean,
    val triState: TriState,
    val icon: (Span.() -> Unit)?,
    val expandedIcon: (Span.() -> Unit)?,
) : HasItem<T> {

    private var internalParent: TreeItem<T>? = null
    private val internalChildren: MutableList<TreeItem<T>> = mutableListOf()

    val children: List<TreeItem<T>>
        get() = internalChildren.toList()

    val hasChildren: Boolean
        get() = internalChildren.isNotEmpty()

    val parent: TreeItem<T>?
        get() = internalParent

    val hasParent: Boolean
        get() = internalParent != null

    fun add(treeItem: TreeItem<T>) {
        treeItem.internalParent = this
        internalChildren.add(treeItem)
    }
}
