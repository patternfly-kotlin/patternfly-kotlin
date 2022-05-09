package org.patternfly.sample

import dev.fritz2.binding.storeOf
import dev.fritz2.dom.Window.clicks
import dev.fritz2.dom.html.render
import org.patternfly.Severity.INFO
import org.patternfly.Step
import org.patternfly.fas
import org.patternfly.notification
import org.patternfly.slider
import org.patternfly.step

internal class SliderSample {

    fun intProgression() {
        render {
            slider(storeOf(23), 0..100)
            slider(storeOf(15), -25..75 step 10) {
                steps { it % 25 == 0 }
                showTicks()
            }
        }
    }

    fun stepProgression() {
        render {
            slider(storeOf(40), Step(10, "10%")..Step(100, "100%") step 10)
        }
    }

    fun customSteps() {
        render {
            slider(
                storeOf(3),
                listOf(
                    Step(0, "0"),
                    Step(1),
                    Step(2, "2"),
                    Step(3),
                    Step(4, "4"),
                    Step(5),
                    Step(6, "6"),
                    Step(7),
                    Step(8, "8")
                )
            )
        }
    }

    fun valueInput() {
        render {
            slider(storeOf(60), 0..100) {
                valueInput()
                valueLabel { "%" }
            }
        }
    }

    fun actions() {
        render {
            slider(storeOf(25), 0..100) {
                leftActions { increase() }
                rightActions {
                    decrease()
                    action {
                        icon("user".fas())
                        clicks handledBy notification(INFO, "Slider action")
                    }
                }
            }
            slider(storeOf(60), 0..100) {
                valueInput()
                valueLabel { "%" }
                rightActions { lock() }
            }
        }
    }
}
