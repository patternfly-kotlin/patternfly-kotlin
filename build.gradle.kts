plugins {
    id("org.jetbrains.kotlin.js") version "1.4-M2" apply false
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.10.0" apply false
}

allprojects {
    group = "org.patternfly"
    version = "0.0.1"

    repositories {
        jcenter()
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.js")
    dependencies {
        "implementation"("org.jetbrains.kotlin:kotlin-stdlib-js")
        "implementation"("dev.fritz2:core:0.6")
    }
}

project(":core") {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "io.gitlab.arturbosch.detekt")
}

project(":showcase") {
    dependencies {
        "implementation"(project(":core"))
    }
}
