import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins.configureEndpoints
import com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins.installPlugins
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    ServerDatabase.configureFromEnvironment()

    Logger.info("Connecting to the database, and creating tables...")
    ServerDatabase.instance

    Logger.info("Starting web server...")
    embeddedServer(Netty, port = 8080) {
        setupApplication()
    }.start(wait = true)
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
