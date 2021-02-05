import org.jetbrains.dokka.Platform
import java.net.URL

plugins {
    kotlin("js") version PluginVersions.js
    id("org.jetbrains.dokka") version PluginVersions.dokka
    id("org.jlleitschuh.gradle.ktlint") version PluginVersions.ktlint
    id("org.jlleitschuh.gradle.ktlint-idea") version PluginVersions.ktlint
    id("io.gitlab.arturbosch.detekt") version PluginVersions.detekt
    `maven-publish`
}

group = Constants.group
version = Constants.version

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://oss.jfrog.org/artifactory/jfrog-dependencies")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    jcenter()
}

dependencies {
    fritz2()
    kotest()
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
                platform.set(Platform.js)
                includes.from("src/main/resources/module.md")
                samples.from("src/main/resources/")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(
                        URL(
                            "https://github.com/patternfly-kotlin/patternfly-fritz2/blob/master/" +
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
            groupId = Constants.group
            artifactId = Constants.name
            version = Constants.version
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            pom {
                name.set("patternfly-fritz2")
                description.set(Constants.description)
                url.set("https://github.com/${Constants.githubRepo}")
                licenses {
                    license {
                        name.set(Constants.license)
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
                    url.set("https://github.com/${Constants.githubRepo}.git")
                    connection.set("scm:git:git://github.com/${Constants.githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/#${Constants.githubRepo}.git")
                }
                issueManagement {
                    url.set("https://github.com/${Constants.githubRepo}/issues")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/${Constants.githubRepo}")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
