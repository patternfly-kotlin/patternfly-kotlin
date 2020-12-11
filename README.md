# PatternFly Fritz2

[![GitHub Super-Linter](https://github.com/patternfly-kotlin/patternfly-fritz2/workflows/lint/badge.svg)](https://github.com/marketplace/actions/super-linter) ![Build Passing](https://github.com/patternfly-kotlin/patternfly-fritz2/workflows/build/badge.svg) [![API Docs](https://img.shields.io/badge/api-docs-brightgreen)](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/) ![Bintray](https://img.shields.io/bintray/v/patternfly-kotlin/patternfly-fritz2/patternfly-fritz2)

PatternFly Fritz2 is a [Kotlin/JS](https://kotl.in/js) implementation of [PatternFly](https://www.org.patternfly.org/) based on [fritz2](https://www.fritz2.dev/).

The goal of this project is to provide all PatternFly components as Kotlin classes. This is done in a way that matches the reactive nature of fritz2. In particular, the components use stores, handlers, and other elements from the fritz2 API.

To get a quick overview what this is all about head over to the PatternFly Fritz2 [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/). It demonstrates the usage of all supported components and also includes more complex demos of data driven components such as [card view](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-card-view/index.html), [data list](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-data-list/index.html) and [data tables](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-data-table/index.html).

To get all details about how to use PatternFly Fritz2 take a look at the [API documentation](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/).

## Get Started

PatternFly Fritz2 is available in Bintray. To use it in your Kotlin/JS project add its dependency to your `gradle.build.kts` file. PatternFly Fritz2 does **not** come with stylesheets, fonts or any other PatternFly asset. You need to add them by yourself using an `npm` dependency and a call to `kotlinext.js.require.require()`.

```kotlin
repositories {
    maven("https://dl.bintray.com/patternfly-kotlin/patternfly-fritz2")
}

dependencies {
    implementation("org.jetbrains:kotlin-extensions:1.0.1-pre.130-kotlin-1.4.21")
    implementation("org.patternfly:patternfly-fritz2:0.1.0")
    implementation(npm("@patternfly/patternfly", "4"))
}
```

```kotlin
import kotlinext.js.require

fun main() {
    require("@patternfly/patternfly/patternfly.css")
    require("@patternfly/patternfly/patternfly-addons.css")
}
```

## Usage

Most PatternFly components are implemented by Kotlin classes and provide factory functions to create them. These functions integrate in the fritz2 DSL and follow a common signature:

1. Parameter(s) specific to the component
1. ID: ID attribute assigned to the component
1. base class: CSS class(es) assigned to the component
1. code block: Code to customize the component

Most of the parameters are optional or have reasonable defaults.

Let's take the [card](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-card/index.html) component as an example. The following code snippet creates a card component with an image and a dropdown in the header, a title, body and footer. See also the card section in the [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/#component;id=card). 

```kotlin
fun main() {
    render {
        card {
            cardHeader {
                img { src("./logo.svg") }
                actions {
                    dropdown<String>(align = RIGHT) {
                        kebabToggle()
                        items {
                            item("Item 1")
                            item("Disabled Item") { disabled = true }
                            separator()
                            item("Separated Item")
                        }
                    }
                }
            }
            cardTitle { +"Title" }
            cardBody { +"Body" }
            cardFooter { +"Footer" }
        }
    }    
}
```

Whenever possible, the components make use of reactive concepts and classes such as stores, handlers and flows. The following example creates a [chip group](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip-group/index.html) component whose [chips](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip/index.html) are backed by a [store](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip-group-store/index.html). The chips are rendered by a [display](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip-group/display.html) function which uses the number of letters to add a [badge](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-badge/index.html) component. See also the chip group section in the [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/#component;id=chip-group).

```kotlin
fun main() {
    data class Word(val text: String, val letters: Int = text.length)

    val store = ChipGroupStore<Word>()
    store.addAll(
        listOf(
            Word("Chip one"),
            Word("Really long chip that goes on and on"),
            Word("Chip three"),
            Word("Chip four"),
            Word("Chip five")
        )
    )

    render {
        chipGroup(store) {
            +"Letters"
            display { word ->
                chip {
                    +word.text
                    badge {
                        value(word.letters)
                    }
                }
            }
        }
    }
}
```

See the [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/) and the [API documentation](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/) for more examples and how to use the various components.
