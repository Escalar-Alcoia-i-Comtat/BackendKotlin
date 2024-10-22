package server.plugins

import database.serialization.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages

/**
 * InstallPlugins method.
 *
 * This method is used to install plugins for the Ktor application.
 * It adds the Locations plugin to the application.
 *
 * @receiver The application on which this method is called.
 */
fun Application.installPlugins() {
    install(CachingHeaders) { configure() }
    install(ConditionalHeaders) { configure() }
    install(ContentNegotiation) { json(Json) }
    install(StatusPages) { configureStatusPages() }
}
