import io.ktor.plugin.features.DockerImageRegistry
import io.ktor.plugin.features.DockerPortMapping
import io.ktor.plugin.features.DockerPortMappingProtocol

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.sentry)
}

group = "com.arnyminerz.escalaralcoiaicomtat.backend"
version = "1.0.26"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val exposedVersion: String by project
val tcnativeVersion: String by project

val osName = System.getProperty("os.name").lowercase()
val tcnativeClassifier = when {
    osName.contains("win") -> "windows-x86_64"
    osName.contains("linux") -> "linux-x86_64"
    osName.contains("mac") -> "osx-x86_64"
    else -> null
}

dependencies {
    // JSON support
    implementation(libs.json)

    // Ktor dependencies
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.locations)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.tlsCertificates)
    implementation(libs.ktor.utils)

    // Ktor client for making requests
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Exposed dependencies
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javaTime)

    // Database engines
    implementation(libs.postgresql)
    // Keep in sync with https://github.com/JetBrains/Exposed/wiki/Database-and-DataSource
    @Suppress("VulnerableLibrariesLocal", "RedundantSuppression")
    implementation(libs.sqlite)

    // SSL Dependencies
    if (tcnativeClassifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion:$tcnativeClassifier")
    } else {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion")
    }

    // For displaying progress bar in terminal
    implementation(libs.progressbar)

    // Crowdin localization
    implementation(libs.crowdin)


    testImplementation(libs.kotlin.test)

    // Add Ktor's testing dependencies
    testImplementation(libs.ktor.test.server)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("ContextReceivers")
        }
    }
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
        localImageName.set("escalaralcoiaicomtat")
        imageTag.set(
            if (System.getenv("IS_PRODUCTION") == "true") version.toString() else "development"
        )
        portMappings.set(
            listOf(
                DockerPortMapping(80, 8080, DockerPortMappingProtocol.TCP)
            )
        )

        externalRegistry.set(
            DockerImageRegistry.dockerHub(
                appName = provider { "escalaralcoiaicomtat" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
    }
}

sentry {
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true

    org = "escalar-alcoia-i-comtat"
    projectName = "server"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

application {
    mainClass.set("MainKt")
}
