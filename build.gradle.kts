@file:Suppress("SpellCheckingInspection")

plugins {
    kotlin("js") version "1.4.10"
    id("org.jetbrains.dokka") version "1.4.10"
    `maven-publish`
}

group = "org.patternfly"
version = "0.1-SNAPSHOT"
val kotestVersion = "4.3.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

kotlin {
    explicitApi()
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }

    sourceSets["main"].dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
        implementation("dev.fritz2:core:0.8-SNAPSHOT")
    }

    sourceSets["test"].dependencies {
        implementation("io.kotest:kotest-framework-api:$kotestVersion")
        implementation("io.kotest:kotest-assertions-core:$kotestVersion")
        implementation("io.kotest:kotest-property:$kotestVersion")
        implementation("io.kotest:kotest-framework-engine:$kotestVersion")
    }
}
