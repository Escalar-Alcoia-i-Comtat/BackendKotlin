package com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins

import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.RootEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create.NewAreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create.NewZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files.RequestFileEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.AreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.ZoneEndpoint
import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/**
 * Configures the endpoints for the application.
 *
 * This method sets up the routing for the application and defines the endpoints.
 *
 * @receiver The application to configure the endpoints for.
 */
fun Application.configureEndpoints() {
    routing {
        get("/") { RootEndpoint.call(this) }
        get("/area/{areaId}") { AreaEndpoint.call(this) }
        post("/area") { NewAreaEndpoint.call(this) }
        get("/zone/{zoneId}") { ZoneEndpoint.call(this) }
        post("/zone") { NewZoneEndpoint.call(this) }
        get("/file/{uuid}") { RequestFileEndpoint.call(this) }
    }
}
