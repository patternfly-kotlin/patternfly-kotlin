@file:Suppress("TooManyFunctions")

package org.patternfly

// ------------------------------------------------------ dsl

public fun <T> tree(block: TreeBuilder<T>.() -> Unit = {}): Tree<T> = TreeBuilder<T>().apply(block).build()

public fun <T> TreeView<T>.tree(block: TreeBuilder<T>.() -> Unit = {}) {
    tree = TreeBuilder<T>().apply(block).build()
}

public fun <T> treeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}): TreeItem<T> =
    TreeItemBuilder(item).apply(block).build()

public fun <T> TreeBuilder<T>.treeItem(item: T, block: TreeItemBuilder<T>.() -> Unit = {}) {
    val builder = TreeItemBuilder(item).apply(block)
    builders.add(builder)
}

public fun <T> TreeItemBuilder<T>.children(block: TreeBuilder<T>.() -> Unit = {}) {
    childrenBuilder.apply(block)
}

// ------------------------------------------------------ type

public class Tree<T> internal constructor(public val roots: List<TreeItem<T>>) {

    public fun find(predicate: (TreeItem<T>) -> Boolean): TreeItem<T>? {
        var result: TreeItem<T>? = null
        val iterator = roots.iterator()
        while (iterator.hasNext() && result == null) {
            val root = iterator.next()
            result = root.find(predicate)
        }
        return result
    }

    public fun findAll(predicate: (TreeItem<T>) -> Boolean): List<TreeItem<T>> {
        val result: MutableList<TreeItem<T>> = mutableListOf()
        for (root in roots) {
            result.addAll(root.findAll(predicate))
        }
        return result
    }
}

public class TreeItem<T> internal constructor(override val item: T) : HasItem<T> {

    private var _parent: TreeItem<T>? = null
    private val _children: MutableList<TreeItem<T>> = mutableListOf()
    internal var fetched: Boolean = false

    public val parent: TreeItem<T>?
        get() = _parent

    public val hasParent: Boolean
        get() = _parent != null

    public val children: List<TreeItem<T>>
        get() = _children

    public val hasChildren: Boolean
        get() = _children.isNotEmpty()

    public val path: List<TreeItem<T>>
        get() {
            val path = mutableListOf<TreeItem<T>>()
            var current = this
            path.add(current)
            while (current.hasParent) {
                current = current.parent!!
                path.add(current)
            }
            return path.reversed()
        }

    internal fun addChild(treeItem: TreeItem<T>) {
        treeItem._parent = this
        _children.add(treeItem)
    }

    public fun find(predicate: (TreeItem<T>) -> Boolean): TreeItem<T>? {
        var result: TreeItem<T>? = null
        if (predicate(this)) {
            result = this
        } else {
            val iterator = _children.iterator()
            while (iterator.hasNext() && result == null) {
                val child = iterator.next()
                result = child.find(predicate)
            }
        }
        return result
    }

    public fun findAll(predicate: (TreeItem<T>) -> Boolean): List<TreeItem<T>> {
        val result: MutableList<TreeItem<T>> = mutableListOf()
        collect(predicate, result)
        return result
    }

    private fun collect(predicate: (TreeItem<T>) -> Boolean, collector: MutableList<TreeItem<T>>) {
        if (predicate(this)) {
            collector.add(this)
        }
        for (child in _children) {
            child.collect(predicate, collector)
        }
    }

    override fun toString(): String = "TreeItem(item=$item)"
}

// ------------------------------------------------------ builder

public class TreeBuilder<T> {

    internal val builders: MutableList<TreeItemBuilder<T>> = mutableListOf()

    internal fun build(): Tree<T> = Tree(builders.map { it.build() })
}

public class TreeItemBuilder<T>(private val item: T) {

    internal val childrenBuilder: TreeBuilder<T> = TreeBuilder()

    internal fun build(): TreeItem<T> {
        val treeItem = TreeItem(item)
        childrenBuilder.builders.forEach { treeItem.addChild(it.build()) }
        return treeItem
    }
}
