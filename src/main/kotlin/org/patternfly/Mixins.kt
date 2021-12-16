package org.patternfly

import dev.fritz2.dom.EventContext
import dev.fritz2.dom.Tag
import dev.fritz2.dom.WithText
import dev.fritz2.dom.html.RenderContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import org.w3c.dom.HTMLElement

/**
 * Interface meant to be implemented by components which have a title. The title can be specified either as a static string or as a [flow][Flow] of strings.
 *
 * Components using this interface can check whether a title has been assigned using [hasTitle] and need to call [applyTitle] to render the title.
 */
public interface WithTitle {
    public val hasTitle: Boolean

    public operator fun String.unaryPlus()

    public fun title(title: String)

    public fun title(title: Flow<String>)

    public fun <T> title(title: Flow<T>)

    public fun Flow<String>.asText()

    public fun <T> Flow<T>.asText()

    public fun applyTitle(target: RenderContext)
}

internal class TitleMixin : WithTitle {
    private var flowTitle: Flow<String>? = null
    private var staticTitle: String? = null

    override val hasTitle: Boolean
        get() = staticTitle != null || flowTitle != null

    override fun String.unaryPlus() {
        staticTitle = this
    }

    override fun title(title: String) {
        staticTitle = title
    }

    override fun title(title: Flow<String>) {
        flowTitle = title
    }

    override fun <T> title(title: Flow<T>) {
        flowTitle = title.map { it.toString() }
    }

    override fun Flow<String>.asText() {
        flowTitle = this
    }

    override fun <T> Flow<T>.asText() {
        flowTitle = this.map { it.toString() }
    }

    override fun applyTitle(target: RenderContext) {
        if ((staticTitle != null || flowTitle != null) && target is WithText<*>) {
            with(target) {
                if (staticTitle != null) {
                    +staticTitle!!
                } else {
                    flowTitle!!.asText()
                }
            }
        }
    }
}

public interface WithElement {
    public fun element(context: Tag<HTMLElement>.() -> Unit)

    public fun applyElement(target: Tag<HTMLElement>)
}

internal class ElementMixin : WithElement {
    private var context: (Tag<HTMLElement>.() -> Unit)? = null

    override fun element(context: Tag<HTMLElement>.() -> Unit) {
        this.context = context
    }

    override fun applyElement(target: Tag<HTMLElement>) {
        context?.invoke(target)
    }
}

public interface WithEvents {
    public val hasEvents: Boolean

    public fun events(context: EventContext<HTMLElement>.() -> Unit)

    public fun applyEvents(target: EventContext<HTMLElement>)
}

internal class EventMixin : WithEvents {
    private var context: (EventContext<HTMLElement>.() -> Unit)? = null

    override val hasEvents: Boolean
        get() = context != null

    override fun events(context: EventContext<HTMLElement>.() -> Unit) {
        this.context = context
    }

    override fun applyEvents(target: EventContext<HTMLElement>) {
        context?.invoke(target)
    }
}

/**
 * Interface meant to be implemented by components which can expand / collapse in some form or another.
 */
public interface WithExpandedStore {

    /**
     * The store which holds the expanded / collapse state.
     */
    public val expandedStore: ExpandedStore

    /**
     * The current expanded / collapsed state.
     */
    public val excos: Flow<Boolean>
}

public class ExpandedStoreMixin(collapsePredicate: CollapsePredicate? = null) : WithExpandedStore {

    override val expandedStore: ExpandedStore = ExpandedStore(collapsePredicate)

    override val excos: Flow<Boolean> = expandedStore.data.drop(1)
}
