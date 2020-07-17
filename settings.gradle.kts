pluginManagement {
    repositories {
        maven("https://dl.bintray.com/kotlin/kotlin-eap")
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
    }
}

rootProject.name = "patternfly-fritz2-parent"
include("impl")
include("showcase")
project(":impl").name = "patternfly-fritz2"
