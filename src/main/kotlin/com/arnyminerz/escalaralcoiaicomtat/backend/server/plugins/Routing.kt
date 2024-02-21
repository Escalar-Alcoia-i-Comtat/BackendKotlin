package com.arnyminerz.escalaralcoiaicomtat.backend.server.plugins

import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointModel.Companion.delete
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointModel.Companion.get
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointModel.Companion.patch
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointModel.Companion.post
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.RootEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.AddBlockEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.DeleteBlockEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.blocking.GetAllBlocksEndpoint
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
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.info.ServerInfoEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy.old.ImportOldDataEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.legacy.old.ImportStatusEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchAreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchPathEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchSectorEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch.PatchZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.AreaEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.LastUpdateEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.PathEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.SectorEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.TreeEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query.ZoneEndpoint
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import io.ktor.server.application.Application
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
        get(RootEndpoint)
        get(ServerInfoEndpoint)

        get(TreeEndpoint)
        get(LastUpdateEndpoint)

        get(AreaEndpoint)
        post(NewAreaEndpoint)
        post(PatchAreaEndpoint)
        delete(DeleteAreaEndpoint)

        get(ZoneEndpoint)
        post(NewZoneEndpoint)
        post(PatchZoneEndpoint)
        delete(DeleteZoneEndpoint)

        get(SectorEndpoint)
        post(NewSectorEndpoint)
        post(PatchSectorEndpoint)
        delete(DeleteSectorEndpoint)

        get(PathEndpoint)
        post(NewPathEndpoint)
        post(PatchPathEndpoint)
        delete(DeletePathEndpoint)

        get(GetAllBlocksEndpoint)
        post(AddBlockEndpoint)
        get(GetBlockEndpoint)
        delete(DeleteBlockEndpoint)
        patch(PatchBlockEndpoint)

        get(RequestFileEndpoint)
        get(DownloadFileEndpoint)

        val enableImporter = EnvironmentVariables.Legacy.Importer.value
        if (enableImporter == "true") {
            Logger.warn(
                "Importer has been enabled through an environment variable. Make sure to disconnect it for production"
            )
            get(ImportOldDataEndpoint)
            get(ImportStatusEndpoint)
        }
    }
}
