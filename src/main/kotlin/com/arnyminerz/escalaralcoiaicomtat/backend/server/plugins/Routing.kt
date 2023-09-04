package com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins

import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.RootEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.AddBlockEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.DeleteBlockEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.GetBlockEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.PatchBlockEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create.NewAreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create.NewPathEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create.NewSectorEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create.NewZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete.DeleteAreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete.DeletePathEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete.DeleteSectorEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.delete.DeleteZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files.DownloadFileEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.files.RequestFileEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy.old.ImportOldDataEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy.old.ImportStatusEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchAreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchPathEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchSectorEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.AreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.PathEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.SectorEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.TreeEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.ZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import io.ktor.server.application.Application
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
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
    if (EnvironmentVariables.Authentication.AuthToken.value == null) {
        Logger.warn("Auth token environment variable not set. Secure endpoints disabled.")
    }

    routing {
        get("/") { RootEndpoint.call(this) }

        get("/tree") { TreeEndpoint.call(this) }

        get("/area/{areaId}") { AreaEndpoint.call(this) }
        post("/area") { NewAreaEndpoint.call(this) }
        post("/area/{areaId}") { PatchAreaEndpoint.call(this) }
        delete("/area/{areaId}") { DeleteAreaEndpoint.call(this) }

        get("/zone/{zoneId}") { ZoneEndpoint.call(this) }
        post("/zone") { NewZoneEndpoint.call(this) }
        post("/zone/{zoneId}") { PatchZoneEndpoint.call(this) }
        delete("/zone/{zoneId}") { DeleteZoneEndpoint.call(this) }

        get("/sector/{sectorId}") { SectorEndpoint.call(this) }
        post("/sector") { NewSectorEndpoint.call(this) }
        post("/sector/{sectorId}") { PatchSectorEndpoint.call(this) }
        delete("/sector/{sectorId}") { DeleteSectorEndpoint.call(this) }

        get("/path/{pathId}") { PathEndpoint.call(this) }
        post("/path") { NewPathEndpoint.call(this) }
        post("/path/{pathId}") { PatchPathEndpoint.call(this) }
        delete("/path/{pathId}") { DeletePathEndpoint.call(this) }

        post("/block/{pathId}") { AddBlockEndpoint.call(this) }
        get("/block/{pathId}") { GetBlockEndpoint.call(this) }
        delete("/block/{blockId}") { DeleteBlockEndpoint.call(this) }
        patch("/block/{blockId}") { PatchBlockEndpoint.call(this) }

        get("/file/{uuid}") { RequestFileEndpoint.call(this) }
        get("/download/{uuid}") { DownloadFileEndpoint.call(this) }

        val enableImporter = EnvironmentVariables.Legacy.Importer.value
        if (enableImporter == "true") {
            Logger.warn(
                "Importer has been enabled through an environment variable. Make sure to disconnect it for production"
            )
            get("/import") { ImportOldDataEndpoint.call(this) }
            get("/import/status") { ImportStatusEndpoint.call(this) }
        }
    }
}
