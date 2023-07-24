plugins {
    kotlin("jvm") version "1.9.0"
    id("io.ktor.plugin") version "2.3.2"
}

group = "com.arnyminerz.escalaralcoiaicomtat.backend"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion: String by project

dependencies {
    // JSON support
    implementation("org.json:json:20230618")

    // Ktor dependencies
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-locations")

    // Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Database engines
    implementation("mysql:mysql-connector-java:8.0.33")


    testImplementation(kotlin("test"))

    // For testing, use SQLite as the database engine
    testImplementation("org.xerial:sqlite-jdbc:3.30.1")

    // Add Ktor's testing dependencies
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)

    sourceSets {
        all {
            languageSettings.enableLanguageFeature("ContextReceivers")
        }
    }
}

application {
    mainClass.set("MainKt")
}
