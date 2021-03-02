import java.net.URL

plugins {
    kotlin("js") version "1.4.30"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.16.0-RC1"
    `maven-publish`
}

group = "org.patternfly"
version = "0.2.0"

val name = "patternfly-fritz2"
val description = "Kotlin implementation of PatternFly 4 based on fritz2"
val license = "Apache-2.0"
val githubRepo = "patternfly-kotlin/patternfly-fritz2"

val fritz2 = "0.9-SNAPSHOT"
val kotest = "4.4.1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

dependencies {
    implementation("dev.fritz2:core:$fritz2")
    testImplementation("io.kotest:kotest-assertions-core:$kotest")
    testImplementation("io.kotest:kotest-property:$kotest")
    testImplementation("io.kotest:kotest-framework-engine:$kotest")
}

kotlin {
    js {
        explicitApi()
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        sourceSets {
            named("main") {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                    useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                    useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
                }
            }
        }
    }
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

tasks {
    dokkaHtml.configure {
        dokkaSourceSets {
            named("main") {
                noJdkLink.set(false)
                noStdlibLink.set(false)
                includeNonPublic.set(false)
                skipEmptyPackages.set(true)
                platform.set(org.jetbrains.dokka.Platform.js)
                includes.from("src/main/resources/module.md")
                samples.from("src/main/resources/")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(
                        URL(
                            "https://github.com/${githubRepo}/blob/master/" +
                                "src/main/kotlin/"
                        )
                    )
                    remoteLineSuffix.set("#L")
                }
                externalDocumentationLink {
                    this.url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
                }
                externalDocumentationLink {
                    this.url.set(URL("https://api.fritz2.dev/core/core/"))
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = name
            version = version
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                name.set(name)
                description.set(description)
                url.set("https://github.com/${githubRepo}")
                licenses {
                    license {
                        name.set(license)
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("hpehl")
                        name.set("Harald Pehl")
                        organization.set("Red Hat")
                        organizationUrl.set("https://developers.redhat.com/")
                    }
                }
                scm {
                    url.set("https://github.com/${githubRepo}.git")
                    connection.set("scm:git:git://github.com/${githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/#${githubRepo}.git")
                }
                issueManagement {
                    url.set("https://github.com/${githubRepo}/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${githubRepo}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
