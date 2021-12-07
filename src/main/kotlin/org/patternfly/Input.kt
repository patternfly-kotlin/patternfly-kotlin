package org.patternfly

import dev.fritz2.dom.html.Input
import kotlinx.coroutines.flow.Flow

/**
 * Sets the tri-state which includes the [ideterminate](http://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/checkbox#indeterminate) state.
 *
 * @receiver an input of type `checkbox`
 */
public fun Input.triState(value: TriState) {
    domNode.checked = value.checked
    domNode.indeterminate = value.indeterminate
}

/**
 * Sets the tri-state which includes the [ideterminate](http://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/checkbox#indeterminate) state.
 *
 * @receiver an input of type `checkbox`
 */
public fun Input.triState(value: Flow<TriState>) {
//    mountSingle(job, value) { v, _ -> triState(v) }
}
