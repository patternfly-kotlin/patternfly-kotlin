import org.jetbrains.dokka.Platform
import java.net.URL

group = "org.patternfly"
version = "0.3.0-SNAPSHOT"

// ------------------------------------------------------ plugins

plugins {
    kotlin("js") version Versions.kotlin
    id("org.jetbrains.dokka") version Versions.dokka
    id("org.jlleitschuh.gradle.ktlint") version Versions.ktlint
    id("org.jlleitschuh.gradle.ktlint-idea") version Versions.ktlint
    id("io.gitlab.arturbosch.detekt") version Versions.detekt
    id("io.github.gradle-nexus.publish-plugin") version Versions.publish
    `maven-publish`
    signing
}

// ------------------------------------------------------ repositories

val repositories = arrayOf(
    "https://oss.sonatype.org/content/repositories/snapshots/",
    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
)

repositories {
    mavenLocal()
    mavenCentral()
    repositories.forEach { maven(it) }
}

// ------------------------------------------------------ dependencies

dependencies {
    implementation("dev.fritz2:core:${Versions.fritz2}")
    testImplementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
    testImplementation("io.kotest:kotest-property:${Versions.kotest}")
    testImplementation("io.kotest:kotest-framework-engine:${Versions.kotest}")
}

// ------------------------------------------------------ kotlin/js

kotlin {
    sourceSets {
        named("main") {
            languageSettings.apply {
                optIn("kotlin.ExperimentalStdlibApi")
                optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
                optIn("kotlinx.coroutines.FlowPreview")
            }
        }
    }
    js(BOTH) {
        explicitApi()
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

// ------------------------------------------------------ ktlint, detekt & dokka

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
                includeNonPublic.set(false)
                noJdkLink.set(false)
                noStdlibLink.set(false)
                platform.set(Platform.js)
                skipEmptyPackages.set(true)
                includes.from("src/main/resources/module.md")
                samples.from("src/main/kotlin/")
                pluginsMapConfiguration.set(
                    mapOf("org.jetbrains.dokka.base.DokkaBase" to """{ "separateInheritedMembers": true}""")
                )
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(
                        URL("https://github.com/${Meta.githubRepo}/blob/main/src/main/kotlin/")
                    )
                    remoteLineSuffix.set("#L")
                }
                externalDocumentationLink {
                    url.set(URL("https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/"))
                }
                externalDocumentationLink {
                    url.set(URL("https://api.fritz2.dev/core/core/"))
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

signing {
    val signingKey = providers
        .environmentVariable("GPG_SIGNING_KEY")
        .forUseAtConfigurationTime()
    val signingPassphrase = providers
        .environmentVariable("GPG_SIGNING_PASSPHRASE")
        .forUseAtConfigurationTime()

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions
            .getByName("publishing") as PublishingExtension
        sign(extension.publications)
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
        sonatype {
            nexusUrl.set(uri(Meta.release))
            snapshotRepositoryUrl.set(uri(Meta.snapshot))
            val ossrhUsername = providers
                .environmentVariable("OSSRH_USERNAME")
                .forUseAtConfigurationTime()
            val ossrhPassword = providers
                .environmentVariable("OSSRH_PASSWORD")
                .forUseAtConfigurationTime()
            if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
                username.set(ossrhUsername.get())
                password.set(ossrhPassword.get())
            }
        }
    }
}
