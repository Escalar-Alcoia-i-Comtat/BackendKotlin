package server.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Locations
import io.ktor.server.plugins.statuspages.StatusPages

/**
 * InstallPlugins method.
 *
 * This method is used to install plugins for the Ktor application.
 * It adds the Locations plugin to the application.
 *
 * @receiver The application on which this method is called.
 */
@OptIn(KtorExperimentalLocationsAPI::class)
fun Application.installPlugins() {
    install(Locations)
    install(StatusPages) { configureStatusPages() }
}
