import org.jetbrains.dokka.Platform
import java.net.URL

// ------------------------------------------------------ plugins

plugins {
    kotlin("js") version "1.4.31"
    id("org.jetbrains.dokka") version "1.4.20"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "9.4.1"
    id("io.gitlab.arturbosch.detekt") version "1.16.0-RC1"
    id("io.github.gradle-nexus.publish-plugin") version "1.0.0"
    `maven-publish`
    signing
}

// ------------------------------------------------------ constants

group = "org.patternfly"
version = "0.3.0-SNAPSHOT"

object Meta {
    const val desc = "Kotlin implementation of PatternFly 4 based on fritz2"
    const val license = "Apache-2.0"
    const val githubRepo = "patternfly-kotlin/patternfly-fritz2"
}

object Versions {
    const val fritz2 = "0.9"
    const val kotest = "4.4.2"
}

val repositories = arrayOf(
    "https://oss.jfrog.org/artifactory/jfrog-dependencies",
    "https://oss.sonatype.org/content/repositories/snapshots/"
)

// ------------------------------------------------------ repositories

repositories {
    mavenLocal()
    mavenCentral()
    repositories.forEach { maven(it) }
    jcenter()
}

// ------------------------------------------------------ dependencies

dependencies {
    implementation("dev.fritz2:core:${Versions.fritz2}")
    testImplementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
    testImplementation("io.kotest:kotest-property:${Versions.kotest}")
    testImplementation("io.kotest:kotest-framework-engine:${Versions.kotest}")
    implementation(kotlin("stdlib-js"))
}

// ------------------------------------------------------ kotlin/js

kotlin {
    js(BOTH) {
        explicitApi()
        sourceSets {
            named("main") {
                languageSettings.apply {
                    useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
                    useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
                    useExperimentalAnnotation("kotlinx.coroutines.FlowPreview")
                }
            }
        }
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
    }
}

// ------------------------------------------------------ source & javadoc

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.get().kotlin)
}

val javadocJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Javadoc JAR"
    archiveClassifier.set("javadoc")
    from(tasks.named("dokkaHtml"))
}

// ------------------------------------------------------ tasks

tasks {
    ktlint {
        filter {
            exclude("**/org/patternfly/sample/**")
        }
    }

    detekt.configure {
        exclude("**/org/patternfly/sample/**")
    }

    dokkaHtml.configure {
        dokkaSourceSets {
            named("main") {
                noJdkLink.set(false)
                noStdlibLink.set(false)
                includeNonPublic.set(false)
                skipEmptyPackages.set(true)
                platform.set(Platform.js)
                includes.from("src/main/resources/module.md")
                samples.from("src/main/kotlin/")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(
                        URL("https://github.com/${Meta.githubRepo}/blob/master/src/main/kotlin/")
                    )
                    remoteLineSuffix.set("#L")
                }
                externalDocumentationLink {
                    this.url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
                }
                externalDocumentationLink {
                    this.url.set(URL("https://api.fritz2.dev/core/core/"))
                }
                perPackageOption {
                    matchingRegex.set("org\\.patternfly\\.sample")
                    suppress.set(true)
                }
            }
        }
    }
}

// ------------------------------------------------------ sign & publish

fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
    configure(configure)

fun Project.publishing(action: PublishingExtension.() -> Unit) =
    configure(action)

signing {
    val signingKey = System.getenv("GPG_SIGNING_KEY").orEmpty()
    val signingPassphrase = System.getenv("GPG_SIGNING_PASSPHRASE").orEmpty()

    if (signingKey.isNotBlank() && signingPassphrase.isNotBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassphrase)
        sign((extensions.getByName("publishing") as PublishingExtension).publications)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set(project.name)
                description.set(Meta.desc)
                url.set("https://github.com/${Meta.githubRepo}")
                licenses {
                    license {
                        name.set(Meta.license)
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
                    url.set("https://github.com/${Meta.githubRepo}.git")
                    connection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/#${Meta.githubRepo}.git")
                }
                issueManagement {
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        create("nexus") {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USERNAME"))
            password.set(System.getenv("OSSRH_PASSWORD"))
        }
    }
}
