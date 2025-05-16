import distribution.Notifier
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import server.plugins.configureEndpoints
import server.plugins.installPlugins
import system.EnvironmentVariables

fun main() {
    if (!EnvironmentVariables.Environment.ServerUUID.isSet) {
        Logger.error("The server UUID is not set. Exiting...")
        return
    }

    ServerDatabase.configureFromEnvironment()

    Logger.info("Connecting to the database, and creating tables...")
    runBlocking { ServerDatabase.instance.initialize() }

    Notifier.getInstance().initialize()

    Logger.info("Starting web server...")
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

/**
 * Performs all the required steps on the target [Application] to that the server is ready to run.
 * 1. Install all the plugin dependencies.
 * 2. Configure all the endpoint listeners.
 */
fun Application.module() {
    installPlugins()
    configureEndpoints()
}
