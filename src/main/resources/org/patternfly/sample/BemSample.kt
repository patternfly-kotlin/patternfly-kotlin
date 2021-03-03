@file:Suppress("UNUSED_VARIABLE")

package org.patternfly.sample

import org.patternfly.classes
import org.patternfly.component
import org.patternfly.modifier
import kotlin.random.Random

internal interface BemSample {

    fun component() {
        "card".component() // pf-c-card
        "card".component("header") // pf-c-card__header
        "card".component("header", "main") // pf-c-card__header-main
    }

    fun classesDsl() {
        val disabled = Random.nextBoolean()
        val classes = classes {
            +"button".component()
            +"plain".modifier()
            +("disabled".modifier() `when` disabled)
        }
    }

    fun classesVararg() {
        val classes = classes(
            "button".component(),
            "plain".modifier(),
            "disabled".modifier()
        )
    }
}
