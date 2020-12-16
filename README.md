# PatternFly Fritz2

[![GitHub Super-Linter](https://github.com/patternfly-kotlin/patternfly-fritz2/workflows/lint/badge.svg)](https://github.com/marketplace/actions/super-linter) [![Build Passing](https://github.com/patternfly-kotlin/patternfly-fritz2/workflows/build/badge.svg)](https://github.com/patternfly-kotlin/patternfly-fritz2/actions) [![API Docs](https://img.shields.io/badge/api-docs-brightgreen)](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/) [![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/patternfly-kotlin/patternfly-fritz2?sort=semver)](https://github.com/patternfly-kotlin/patternfly-fritz2/releases/latest) [ ![Download](https://api.bintray.com/packages/patternfly-kotlin/patternfly-fritz2/patternfly-fritz2/images/download.svg) ](https://bintray.com/patternfly-kotlin/patternfly-fritz2/patternfly-fritz2/_latestVersion) [![Gitter](https://img.shields.io/gitter/room/patternfly-kotlin/patternfly-fritz2)](https://gitter.im/patternfly-kotlin/patternfly-fritz2)

PatternFly Fritz2 is a [Kotlin/JS](https://kotl.in/js) implementation of [PatternFly](https://www.org.patternfly.org/) based on [fritz2](https://www.fritz2.dev/).

The goal of this project is to provide all PatternFly components in Kotlin/JS. This is done in a way that matches the reactive nature of fritz2. In particular, the components use [stores](https://api.fritz2.dev/core/core/dev.fritz2.binding/-store/index.html), [handlers](https://api.fritz2.dev/core/core/dev.fritz2.binding/-handler/index.html), and other elements from the fritz2 API.

To get a quick overview what this is all about head over to the PatternFly Fritz2 [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/). It demonstrates the usage of all supported components and also includes more complex demos of data driven components such as [card view](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-card-view/index.html), [data list](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-data-list/index.html) and [data tables](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-data-table/index.html).

To get all details about how to use PatternFly Fritz2 take a look at the [API documentation](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/).

## Get Started

PatternFly Fritz2 is available in Bintray. To use it in your Kotlin/JS project add its dependency to your `gradle.build.kts` file. 

```kotlin
repositories {
    maven("https://dl.bintray.com/patternfly-kotlin/patternfly-fritz2")
}

dependencies {
    implementation("org.jetbrains:kotlin-extensions:<version>")
    implementation("org.patternfly:patternfly-fritz2:0.1.0")
    implementation(npm("@patternfly/patternfly", "4"))
}
```

PatternFly Fritz2 does **not** come with stylesheets, fonts or any other PatternFly assets. You need to add them by yourself using an `npm` dependency and a call to `kotlinext.js.require.require()`.

```kotlin
import kotlinext.js.require

fun main() {
    require("@patternfly/patternfly/patternfly.css")
    require("@patternfly/patternfly/patternfly-addons.css")

    document.body?.appendChild(
        renderElement {
            page {
                pageHeader {
                    brand {
                        home("#home")
                        img("/assets/logo.svg")
                    }
                    headerTools {
                        notificationBadge()
                    }
                }
                pageSidebar {
                    sidebarBody {
                        verticalNavigation(router) {
                            navigationItems {
                                navigationItem("#item1", "Item 1")
                                navigationItem("#item2", "Item 2")
                            }
                        }
                    }
                }
                pageMain {
                    pageSection {
                        h1 { +"Welcome" }
                        p { +"Lorem ipsum" }
                    }
                    pageSection {
                        +"Another section"
                    }
                }
            }
        }.domNode
    )
}
```

## Usage

Most PatternFly components are implemented by Kotlin classes and created by factory functions. These functions integrate in the fritz2 DSL and follow a common pattern:

1. Parameter(s) specific to the component
1. `id: String? = null` ID attribute assigned to the component
1. `baseClass: String? = null` CSS class(es) added to the list of classes of the component
1. `content` code block to customize the component

Most of the parameters are optional and have reasonable defaults.

Let's take the [card](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-card/index.html) component as an example. The following code snippet creates a card component with an image and a dropdown in the header, a title, body and footer. 

See also the card section in the [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/#component;id=card). 

```kotlin
fun main() {
    render {
        card {
            cardHeader {
                img { src("./logo.svg") }
                cardAction {
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

Whenever possible, the components make use of reactive concepts and classes such as [stores](https://api.fritz2.dev/core/core/dev.fritz2.binding/-store/index.html), [handlers](https://api.fritz2.dev/core/core/dev.fritz2.binding/-handler/index.html) and [flows](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-flow/index.html). The following example creates a [chip group](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip-group/index.html) component whose [chips](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip/index.html) are backed by a [store](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip-group-store/index.html) and rendered by a [display](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-chip-group/display.html) function. The function uses the number of letters to add a [badge](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-badge/index.html) component to the chip. When a chip is removed from the group a info [notification](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-notification/index.html) is [added](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-notification/-companion/add.html) to the [notification store](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/-notification-store/index.html) which in turn is fetched by the [toast alert group](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/org.patternfly/add-toast-alert-group.html). 

See also the chip group section in the [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/#component;id=chip-group).

```kotlin
fun main() {
    data class Word(val text: String, val letters: Int = text.length)

    val store = ChipGroupStore<Word> { Id.build(it.text) }.apply {
        addAll(
            listOf(
                Word("Chip one"),
                Word("Really long chip that goes on and on"),
                Word("Chip three"),
                Word("Chip four"),
                Word("Chip five")
            )
        )
        removes handledBy Notification.add { word ->
            info("You removed ${word.text}.")
        }
    }

    document.addToastAlertGroup()
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

To see more components in action, take a look at the [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/). To learn how to use the components, read the [API documentation](https://patternfly-kotlin.github.io/patternfly-fritz2/patternfly-fritz2/).

## Get Involved

PatternFly Fritz2 is still under development. The API might change and things might not work as expected. Please give it a try and share your feedback. Join the chat at [Gitter](https://gitter.im/patternfly-kotlin/patternfly-fritz2) or use the GitHub [issues](https://github.com/patternfly-kotlin/patternfly-fritz2/issues) to file bugs or open feature requests. 

Of course, you're very welcome to [contribute](CONTRIBUTING.md) to PatternFly Fritz2.  
