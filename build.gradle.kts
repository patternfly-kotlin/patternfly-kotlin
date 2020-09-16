import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "1.4.10"
    `maven-publish`
}

group = "org.patternfly"
version = "0.1-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
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
        val commonMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.8-SNAPSHOT")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
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
