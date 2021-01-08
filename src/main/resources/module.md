# Module patternfly-fritz2

PatternFly Fritz2 is a 💯 Kotlin implementation of [PatternFly](https://www.patternfly.org/) based on [fritz2](https://www.fritz2.dev/) targeting [Kotlin/JS](https://kotl.in/js).

The goal of this project is to provide all PatternFly components in Kotlin. This is done in a way that matches the reactive nature of fritz2. In particular, the components use [stores](https://api.fritz2.dev/core/core/dev.fritz2.binding/-store/index.html), [handlers](https://api.fritz2.dev/core/core/dev.fritz2.binding/-handler/index.html), and other elements from the [fritz2 API](https://api.fritz2.dev/core/core/index.html).

All components in PatternFly Fritz2 are completely implemented in Kotlin and are created by factory functions. These functions integrate in the fritz2 DSL and follow a common pattern:

1. Parameter(s) specific to the component
1. `id: String? = null` ID attribute assigned to the component
1. `baseClass: String? = null` CSS class(es) added to the list of classes of the component
1. `content` code block to customize the component

Most of the parameters are optional and have reasonable defaults.

# Package org.patternfly

This package contains classes and factory functions to create and use the PatternFly components.

# Package org.patternfly.dom

This package contains helper classes to work with the DOM, HTML elements and fritz2 tags. 
