plugins {
    id("org.jlleitschuh.gradle.ktlint")
//    id("io.gitlab.arturbosch.detekt")
}

ktlint {
    disabledRules.set(setOf("import-ordering"))
}

/*
detekt {
    toolVersion = "1.10.0"
}
*/

kotlin {
    js {
        browser { }
    }
}
