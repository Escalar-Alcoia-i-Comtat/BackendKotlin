plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.ktor)
    alias(libs.plugins.sentry)
}

group = "com.arnyminerz.escalaralcoiaicomtat.backend"
version = file("package/version.txt").readText().trim()

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val tcnativeVersion: String by project

val osName = System.getProperty("os.name").lowercase()
val tcnativeClassifier = when {
    osName.contains("win") -> "windows-x86_64"
    osName.contains("linux") -> "linux-x86_64"
    osName.contains("mac") -> "osx-x86_64"
    else -> null
}

dependencies {
    // Ktor dependencies
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.locations)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.tlsCertificates)
    implementation(libs.ktor.utils)

    // Ktor client for making requests
    implementation(libs.ktor.client.contentNegotiation)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Kotlin Serialization & Ktor Serialization
    implementation(libs.kotlin.serializationJson)
    implementation(libs.ktor.serializationJson)

    // Exposed dependencies
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javaTime)
    implementation(libs.exposed.json)

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

    // WebP image support
    implementation(libs.imageio.webp)

    // Firebase
    implementation(libs.firebase.admin)


    testImplementation(libs.kotlin.test)

    // Add Ktor's testing dependencies
    testImplementation(libs.ktor.test.server)
}

fun getSecret(key: String): String? {
    val file = file("secrets.env")
    if (!file.exists()) error("Secrets file not found")
    val secrets = file.readLines()
    return secrets.find { it.startsWith(key) }?.split("=")?.get(1)
}

tasks.test {
    useJUnitPlatform()

    val sentryDsn: String? = System.getenv("SENTRY_DSN_TESTS") ?: getSecret("SENTRY_DSN_TESTS")
    environment("SENTRY_DSN_TESTS", sentryDsn ?: error("Sentry DSN not configured for tests"))
}

kotlin {
    jvmToolchain(22)

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("ContextReceivers")
            resources.srcDir(file("package"))
        }
    }
}

ktor {
    fatJar {

    }
}

if (System.getenv("SENTRY_DISABLE") != "true") {
    sentry {
        // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
        // This enables source context, allowing you to see your source
        // code as part of your stack traces in Sentry.
        includeSourceContext = true

        org = "escalar-alcoia-i-comtat"
        projectName = "server"
        authToken = System.getenv("SENTRY_AUTH_TOKEN")
    }
} else {
    println("Warning: Sentry is disabled.")
}

application {
    mainClass.set("MainKt")
}

kover {
    reports {
        filters {
            excludes {
                annotatedBy("KoverIgnore")
            }
        }

        verify {
            // verification rules for all reports
        }
    }
}
