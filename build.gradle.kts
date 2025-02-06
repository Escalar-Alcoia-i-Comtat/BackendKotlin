plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.ktor)
}

group = "com.arnyminerz.escalaralcoiaicomtat.backend"
version = file("package/version.txt").readText().trim()

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    // Ktor dependencies
    implementation(libs.ktor.server.cachingHeaders)
    implementation(libs.ktor.server.conditionalHeaders)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.statusPages)
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
    implementation(libs.h2)

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

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)

    sourceSets {
        all {
            resources.srcDir(file("package"))
        }
    }
}

ktor {
    fatJar {

    }
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
