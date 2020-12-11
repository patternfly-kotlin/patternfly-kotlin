# PatternFly Fritz2

![Build Passing](https://github.com/patternfly-kotlin/patternfly-fritz2/workflows/build/badge.svg) [![GitHub Super-Linter](https://github.com/patternfly-kotlin/patternfly-fritz2/workflows/lint/badge.svg)](https://github.com/marketplace/actions/super-linter) ![Bintray](https://img.shields.io/bintray/v/patternfly-kotlin/patternfly-fritz2/patternfly-fritz2) [![API Docs](https://img.shields.io/badge/api-docs-brightgreen)](https://patternfly-kotlin.github.io/patternfly-fritz2/)

PatternFly Fritz2 is a [Kotlin/JS](https://kotl.in/js) implementation of [PatternFly](https://www.org.patternfly.org/) based on [fritz2](https://www.fritz2.dev/). 

The goal of this project is to provide all components of PatternFly in Kotlin. This is done in a way that matches the reactive nature of fritz2. In particular, the components use stores, handlers, and other elements from the fritz2 API.

To get a quick overview what this is all about head over to the PatternFly Fritz2 [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/). It demonstrates the usage of all supported components and also includes more complex demos of data driven components such as card view, data list and data tables. 

To get all details about how to use PatternFly Fritz2 take a look at the [API documentation](https://patternfly-kotlin.github.io/patternfly-fritz2/).

## Get Started

PatternFly Fritz2 is available in Bintray. To use it in your Kotlin/JS project add its dependency to your `gradle.build.kts` file. PatternFly Fritz2 does **not** come with stylesheets, fonts or any other PatternFly asset. You need to add them by yourself using an `npm` dependency and a call to `require()` in your main function.   

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

Most components are created using functions that takes parameters and return the component class. The functions act as a factory which integrates in the fritz2 DSL. The parameters passed to the factory functions follow a common pattern:

1. Parameter(s) specific to the component
2. ID: ID attribute assigned to the component
3. base class: CSS class(es) assigned to the component
4. code block: Code to customize the component

