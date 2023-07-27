import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins.configureEndpoints
import com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins.installPlugins
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngineEnvironmentBuilder
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import java.io.File
import java.security.KeyStore

const val HTTP_PORT = 8080
const val HTTPS_PORT = 8443

fun main() {
    ServerDatabase.configureFromEnvironment()

    Logger.info("Connecting to the database, and creating tables...")
    ServerDatabase.instance

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
    val keystoreKeyPassword = System.getenv("SSL_KEYSTORE_KEY_PASSWORD")
    val keystorePassword = System.getenv("SSL_KEYSTORE_PASSWORD")
    val keystoreAlias = System.getenv("SSL_KEYSTORE_ALIAS") ?: "certificate"

    if (keystoreFileName == null) {
        Logger.info("SSL_KEYSTORE_FILE is not defined. SSL will be disabled.")
        return
    }
    if (keystorePassword == null) {
        Logger.info("SSL_KEYSTORE_PASSWORD is not defined. SSL will be disabled.")
        return
    }
    if (keystoreKeyPassword == null) {
        Logger.info("SSL_KEYSTORE_KEY_PASSWORD is not defined. SSL will be disabled.")
        return
    }

    val keystoreSrcDir = File(keystoreDirName)
    val keystoreFile = File(keystoreSrcDir, keystoreFileName)

    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        keystoreFile.inputStream().use {
            load(it, keystorePassword.toCharArray())
        }
    }
    sslConnector(
        keyStore,
        keystoreAlias,
        keyStorePassword = { keystorePassword.toCharArray() },
        privateKeyPassword = { keystoreKeyPassword.toCharArray() }
    ) {
        port = HTTPS_PORT
    }
}
