import io.ktor.plugin.features.DockerImageRegistry
import io.ktor.plugin.features.DockerPortMapping
import io.ktor.plugin.features.DockerPortMappingProtocol
import io.ktor.plugin.features.JreVersion

plugins {
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.3"
    id("org.jetbrains.kotlinx.kover") version "0.7.3"
}

group = "com.arnyminerz.escalaralcoiaicomtat.backend"
version = "1.0.9-SNAPSHOT"

repositories {
    mavenCentral()
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
    implementation("org.json:json:20230618")

    // Ktor dependencies
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-locations")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-network-tls-certificates")
    implementation("io.ktor:ktor-utils")

    // Ktor client for making requests
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-cio")

    // Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Database engines
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")

    // SSL Dependencies
    if (tcnativeClassifier != null) {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion:$tcnativeClassifier")
    } else {
        implementation("io.netty:netty-tcnative-boringssl-static:$tcnativeVersion")
    }

    // For displaying progress bar in terminal
    implementation("me.tongfei:progressbar:0.9.4")


    testImplementation(kotlin("test"))

    // Add Ktor's testing dependencies
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
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
        jreVersion.set(JreVersion.JRE_17)
        localImageName.set("escalaralcoiaicomtat")
        imageTag.set(version.toString())
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

application {
    mainClass.set("MainKt")
}
