package org.patternfly

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.Input
import dev.fritz2.elemento.debug
import kotlinx.coroutines.flow.Flow

public fun Input.triState(value: TriState) {
    domNode.checked = value.checked
    domNode.indeterminate = value.indeterminate
}

public fun Input.triState(value: Flow<TriState>) {
    mountSingle(job, value) { v, _ -> triState(v) }
}
