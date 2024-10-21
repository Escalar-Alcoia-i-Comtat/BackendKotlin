package server.plugins

import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.routing.Route

fun Route.configureCORS() {
    install(CORS) {
        allowHost("localhost:8080")
        allowHost("app.escalaralcoiaicomtat.org")
        allowHost("web.escalaralcoiaicomtat.org")
    }
}
