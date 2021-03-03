package org.patternfly.sample

import dev.fritz2.dom.html.render
import dev.fritz2.lenses.IdProvider
import org.patternfly.ItemsStore
import org.patternfly.dataList
import org.patternfly.dataListItem
import org.patternfly.dataListRow
import org.patternfly.dom.Id

internal interface WithIdProviderSample {

    fun useItemId() {
        render {
            // this ID provider will be used below
            val idProvider: IdProvider<String, String> = { Id.build(it) }

            dataList(ItemsStore(idProvider)) {
                display {
                    dataListItem(it) {
                        dataListRow(id = itemId(it)) {
                            +it
                        }
                    }
                }
            }
        }
    }
}
