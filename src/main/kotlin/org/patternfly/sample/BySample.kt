package org.patternfly.sample

import org.patternfly.dom.AttributeOperator.STARTS_WITH
import org.patternfly.dom.By

internal class BySample {

    fun complex() {
        By.group(
            By.id("main")
                .desc(
                    By.data("listItem", "foo")
                        .desc(
                            By.element("a")
                                .and(By.attribute("href", "https://", STARTS_WITH))
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
