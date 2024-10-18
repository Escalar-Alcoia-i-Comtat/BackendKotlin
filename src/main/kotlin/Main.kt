import diagnostics.Diagnostics
import distribution.Notifier
import io.ktor.server.application.Application
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import localization.Localization
import server.plugins.configureEndpoints
import server.plugins.installPlugins
import system.EnvironmentVariables

const val HTTP_PORT = 8080

fun main() {
    if (!EnvironmentVariables.Environment.ServerUUID.isSet) {
        Logger.error("The server UUID is not set. Exiting...")
        return
    }

    ServerDatabase.configureFromEnvironment()

    Logger.info("Connecting to the database, and creating tables...")
    runBlocking { ServerDatabase.instance.initialize() }

    Logger.info("Initializing Crowdin connection...")
    Localization.init()

    Notifier.getInstance().initialize()

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
