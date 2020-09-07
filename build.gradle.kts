plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0"
    `maven-publish`
}

group = "org.patternfly"
version = "0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    jcenter()
}

kotlin {
    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.8-SNAPSHOT")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
