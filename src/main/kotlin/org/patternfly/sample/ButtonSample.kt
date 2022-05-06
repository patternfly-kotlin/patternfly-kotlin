package org.patternfly.sample

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.render
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.patternfly.ButtonSize.callToAction
import org.patternfly.ButtonSize.small
import org.patternfly.ButtonVariant.control
import org.patternfly.ButtonVariant.danger
import org.patternfly.ButtonVariant.plain
import org.patternfly.ButtonVariant.primary
import org.patternfly.ButtonVariant.secondary
import org.patternfly.ButtonVariant.tertiary
import org.patternfly.Severity.INFO
import org.patternfly.classes
import org.patternfly.clickButton
import org.patternfly.fas
import org.patternfly.layout
import org.patternfly.linkButton
import org.patternfly.modifier
import org.patternfly.notification
import org.patternfly.pushButton

internal class ButtonSample {

    fun pushButton() {
        render {
            pushButton(primary) { +"Button" }
        }
    }

    fun clickButton() {
        render {
            clickButton(secondary) {
                +"Click me"
            } handledBy notification(INFO, "Score!")
        }
    }

    fun linkButton() {
        render {
            linkButton(secondary) {
                +"PatternFly"
                href("https://patternfly.org")
            }
        }
    }

    fun content() {
        render {
            pushButton(tertiary) {
                content {
                    div(baseClass = "split".layout()) {
                        div(baseClass = "split".layout("item")) {
                            +"content"
                        }
                        div(
                            baseClass = classes(
                                "split".layout("item"),
                                "fill".modifier()
                            )
                        ) {
                            +"fill"
                        }
                        div(baseClass = "split".layout("item")) {
                            +"content"
                        }
                    }
                }
            }
        }
    }

    fun titleAndIcon() {
        render {
            pushButton(primary) { +"Title only" }
            pushButton(plain) { icon("user".fas()) }
            pushButton(secondary, danger) {
                +"Title and icon"
                icon("user".fas())
            }
            pushButton(control) {
                icon("user".fas())
                +"Icon and title"
            }
        }
    }

    fun loading() {
        val onOff = object : RootStore<Boolean>(false) {
            val toggle = handle { !it }
        }
        render {
            clickButton(primary) {
                title("Click to start loading")
                loading(onOff.data, "Click to stop loading")
            } handledBy onOff.toggle
        }
    }

    fun loadingTitle() {
        val download = object : RootStore<Unit>(Unit) {
            val state = MutableStateFlow(0)
            val start = handle {
                state.value = 1
                for (i in 2..11) {
                    delay(750)
                    state.value = i
                }
            }
            val inProgress = state.map { it in 1..10 }
            val percentage = state.map { "${it * 10}%" }
        }
        render {
            clickButton(secondary) {
                icon("download".fas())
                loading(download.inProgress)
            } handledBy download.start
            clickButton(secondary) {
                +"Download with progress"
                loading(download.inProgress) {
                    div(baseClass = "fmt-percentage") {
                        download.percentage.renderText()
                    }
                }
            } handledBy download.start
        }
    }

    fun sizes() {
        render {
            pushButton(secondary) {
                size(small)
                +"Small"
            }
            pushButton(secondary) {
                +"Normal"
            }
            pushButton(secondary) {
                size(callToAction)
                +"Call to action"
            }
        }
    }
}
