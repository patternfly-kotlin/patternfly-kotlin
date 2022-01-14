package org.patternfly.sample

import dev.fritz2.dom.html.render
import org.patternfly.PageInfo
import org.patternfly.pagination

internal class PaginationSample {

    fun pageInfo() {
        render {
            pagination(PageInfo(10, 0, 73))
        }
    }
}
