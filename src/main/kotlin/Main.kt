import diagnostics.Diagnostics
import distribution.DeviceNotifier
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngineEnvironmentBuilder
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.util.decodeBase64String
import java.io.File
import java.security.KeyStore
import kotlinx.coroutines.runBlocking
import localization.Localization
import server.plugins.configureEndpoints
import server.plugins.installPlugins
import system.EnvironmentVariables

const val HTTP_PORT = 8080
const val HTTPS_PORT = 8443

fun main() {
    if (!EnvironmentVariables.Environment.ServerUUID.isSet) {
        Logger.error("The server UUID is not set. Exiting...")
        return
    }

    ServerDatabase.configureFromEnvironment()

    Logger.info("Connecting to the database, and creating tables...")
    ServerDatabase.instance.initialize()

    Logger.info("Initializing Crowdin connection...")
    Localization.init()

    DeviceNotifier.initialize()

    runBlocking {
        Localization.synchronizePathDescriptions()
    }

    if (Diagnostics.init()) {
        Logger.info("Sentry integration is enabled!")
    } else {
        Logger.info("Sentry not configured, won't enable feature...")
    }

    val environment = applicationEngineEnvironment {
        connector { port = HTTP_PORT }
        configureSsl()
        module(Application::setupApplication)
    }

    Logger.info("Starting web server...")
    embeddedServer(Netty, environment).start(wait = true)
}

/**
 * Performs all the required steps on the target [Application] to that the server is ready to run.
 * 1. Install all the plugin dependencies.
 * 2. Configure all the endpoint listeners.
 */
fun Application.setupApplication() {
    installPlugins()
    configureEndpoints()
}

fun ApplicationEngineEnvironmentBuilder.configureSsl() {
    val keystoreDirName = System.getenv("SSL_KEYSTORE_DIR") ?: "/var/lib/escalaralcoiaicomtat/keystore"
    val keystoreFileName = System.getenv("SSL_KEYSTORE_FILE")
    val keystorePassword = System.getenv("SSL_KEYSTORE_PASSWORD")
    val keystoreAlias = System.getenv("SSL_KEYSTORE_ALIAS") ?: "certificate"

    val certsDirName = System.getenv("SSL_CERTS_DIR") ?: "/var/lib/escalaralcoiaicomtat/certs"
    val certKeyFileName = System.getenv("SSL_CERT_KEY_FILE")

    if (keystoreFileName == null) {
        Logger.info("SSL_KEYSTORE_FILE is not defined. SSL will be disabled.")
        return
    }
    if (keystorePassword == null) {
        Logger.info("SSL_KEYSTORE_PASSWORD is not defined. SSL will be disabled.")
        return
    }
    if (certKeyFileName == null) {
        Logger.info("SSL_CERT_KEY_FILE is not defined. SSL will be disabled.")
        return
    }

    val keystoreSrcDir = File(keystoreDirName)
    val keystoreFile = File(keystoreSrcDir, keystoreFileName)

    val certsSrcDir = File(certsDirName)
    val certKeyFile = File(certsSrcDir, certKeyFileName)

    val privateKeyPassword = certKeyFile.readText().decodeBase64String()

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        keystoreFile.inputStream().use {
            load(it, keystorePassword.toCharArray())
        }
    }
    sslConnector(
        keyStore,
        keystoreAlias,
        keyStorePassword = { keystorePassword.toCharArray() },
        privateKeyPassword = { privateKeyPassword.toCharArray() }
    ) {
        port = HTTPS_PORT
    }
}
