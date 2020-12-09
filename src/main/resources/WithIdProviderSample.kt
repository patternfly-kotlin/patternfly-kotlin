package org.patternfly

import dev.fritz2.dom.html.render
import org.patternfly.dom.Id
import dev.fritz2.lenses.IdProvider

internal interface WithIdProviderSample {

    fun useItemId() {
        render {
            // this ID provider will be used below
            val idProvider: IdProvider<String, String> = { Id.build(it) }

            dataList(ItemStore(idProvider)) {
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
