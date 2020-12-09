package org.patternfly.dom

import dev.fritz2.elemento.AttributeOperator
import dev.fritz2.elemento.By

internal interface BySample {

    fun complex() {
        By.group(
            By.id("main")
                .desc(
                    By.data("listItem", "foo")
                        .desc(
                            By.element("a")
                                .and(By.attribute("href", "https://", AttributeOperator.STARTS_WITH))
                                .child(By.classname("fas", "fa-check"))
                        )
                ),
            By.classname("external").and(By.attribute("hidden"))
        )
    }

    fun and() {
        By.element("button").and(By.classname("primary"))
        By.element("input").and(By.attribute("type", "checkbox"))
    }
}
