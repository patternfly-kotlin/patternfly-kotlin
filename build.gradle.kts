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

tasks.withType<KotlinCompile>().all {
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

kotlin {
    jvm {
        tasks.withType<KotlinCompile> {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        }
        tasks.withType<Test> {
            useJUnitPlatform()
        }
    }
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
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.8-SNAPSHOT")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.kotest:kotest-framework-api:4.2.5")
                implementation("io.kotest:kotest-assertions-core:4.2.5")
                implementation("io.kotest:kotest-property:4.2.5")
                implementation("io.kotest:kotest-framework-engine:4.2.5")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("dev.fritz2:core:0.8-SNAPSHOT")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-framework-api:4.2.5")
                implementation("io.kotest:kotest-assertions-core:4.2.5")
                implementation("io.kotest:kotest-property:4.2.5")
                implementation("io.kotest:kotest-framework-engine:4.2.5")
                implementation("io.kotest:kotest-runner-junit5:4.2.5")
            }
        }
    }
}
