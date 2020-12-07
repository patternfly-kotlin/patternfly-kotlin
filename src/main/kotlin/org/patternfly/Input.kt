package org.patternfly

import dev.fritz2.binding.mountSingle
import dev.fritz2.dom.html.Input
import kotlinx.coroutines.flow.Flow

public fun Input.triState(value: TriState) {
    checked(value.checked)
    indeterminate(value.indeterminate)
}

public fun Input.triState(value: Flow<TriState>) {
    mountSingle(job, value) { v, _ -> triState(v) }
}
