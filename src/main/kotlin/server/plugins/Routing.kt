package server.plugins

import Logger
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import server.endpoints.EndpointModel.Companion.delete
import server.endpoints.EndpointModel.Companion.get
import server.endpoints.EndpointModel.Companion.patch
import server.endpoints.EndpointModel.Companion.post
import server.endpoints.RootEndpoint
import server.endpoints.blocking.AddBlockEndpoint
import server.endpoints.blocking.DeleteBlockEndpoint
import server.endpoints.blocking.GetAllBlocksEndpoint
import server.endpoints.blocking.GetBlockEndpoint
import server.endpoints.blocking.PatchBlockEndpoint
import server.endpoints.create.NewAreaEndpoint
import server.endpoints.create.NewPathEndpoint
import server.endpoints.create.NewSectorEndpoint
import server.endpoints.create.NewZoneEndpoint
import server.endpoints.delete.DeleteAreaEndpoint
import server.endpoints.delete.DeletePathEndpoint
import server.endpoints.delete.DeleteSectorEndpoint
import server.endpoints.delete.DeleteZoneEndpoint
import server.endpoints.files.DownloadFileEndpoint
import server.endpoints.files.RequestFileEndpoint
import server.endpoints.info.ServerInfoEndpoint
import server.endpoints.patch.PatchAreaEndpoint
import server.endpoints.patch.PatchPathEndpoint
import server.endpoints.patch.PatchSectorEndpoint
import server.endpoints.patch.PatchZoneEndpoint
import server.endpoints.query.AreaEndpoint
import server.endpoints.query.LastUpdateEndpoint
import server.endpoints.query.PathEndpoint
import server.endpoints.query.SectorEndpoint
import server.endpoints.query.TreeEndpoint
import server.endpoints.query.ZoneEndpoint
import system.EnvironmentVariables

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
        configureCORS()

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
    }
}
