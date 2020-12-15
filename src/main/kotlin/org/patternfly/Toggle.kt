package org.patternfly

import dev.fritz2.dom.WithDomNode
import kotlinx.browser.document
import kotlinx.coroutines.flow.Flow
import org.w3c.dom.Comment
import org.w3c.dom.Node

/**
 * Common interface for [Dropdown] and [OptionsMenu] toggle variants.
 */
public interface Toggle<T, out N : Node> : WithDomNode<N> {

    /**
     * Disables or enables the toggle.
     */
    public fun disabled(value: Boolean)

    /**
     * Disables or enables the toggle based on the values from the flow.
     */
    public fun disabled(value: Flow<Boolean>)
}

internal class RecordingToggle<T> : Toggle<T, Comment> {

    private var recordedBoolean: Boolean? = null
    private var recordedFlow: Flow<Boolean>? = null
    override val domNode: Comment = document.createComment("noop toggle")

    override fun disabled(value: Boolean) {
        recordedBoolean = value
    }

    override fun disabled(value: Flow<Boolean>) {
        recordedFlow = value
    }

    internal fun <N : Node> playback(toggle: Toggle<T, N>) {
        recordedBoolean?.let { toggle.disabled(it) }
        recordedFlow?.let { toggle.disabled(it) }
    }
}
