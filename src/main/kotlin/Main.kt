import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.localization.Localization
import com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins.configureEndpoints
import com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins.installPlugins
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngineEnvironmentBuilder
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.util.decodeBase64String
import io.sentry.Sentry
import java.io.File
import java.security.KeyStore
import kotlinx.coroutines.runBlocking

const val HTTP_PORT = 8080
const val HTTPS_PORT = 8443

fun main() {
    ServerDatabase.configureFromEnvironment()

    Logger.info("Connecting to the database, and creating tables...")
    ServerDatabase.instance

    Logger.info("Initializing Crowdin connection...")
    Localization.init()

    runBlocking {
        Localization.synchronizePathDescriptions()
    }

    EnvironmentVariables.Diagnostics.SentryDsn.value?.let { dsn ->
        Logger.info("Enabling Sentry integration...")

        Sentry.init { options ->
            options.dsn = dsn
            options.tracesSampleRate = 1.0
            options.isDebug = EnvironmentVariables.Environment.IsProduction.value != "true"
        }

    } ?: Logger.info("Sentry not configured, disabling feature...")

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
