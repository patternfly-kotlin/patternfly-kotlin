# PatternFly Fritz2

PatternFly Fritz2 is a [Kotlin/JS](https://kotl.in/js) implementation of [PatternFly](https://www.org.patternfly.org/) based on [fritz2](https://www.fritz2.dev/). 

The goal of this project is to provide all components of PatternFly in Kotlin. This is done in a way so that it matches the reactive nature of fritz2. In particular, the components build on stores, handlers, and other elements from the fritz2 API.

To get a quick overview what this is all about head over to the PatternFly Fritz2 [showcase](https://patternfly-kotlin.github.io/patternfly-fritz2-showcase/).

## Get Started

PatternFly Fritz2 is available in Bintray. To use it in your Kotlin/JS project add its dependency to your `gradle.build.kts` file. PatternFly Fritz2 does **not** come with stylesheets, fonts or any other PatternFly asset. You need to add them by yourself using an `npm` dependency and a call to `require()` in your main function.   

```kotlin
repositories {
    maven("https://dl.bintray.com/patternfly-kotlin/patternfly-fritz2")
}

dependencies {
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

## API

The API documentation for PatternFly Fritz2 is available at 

### Factory Functions

### Stores

