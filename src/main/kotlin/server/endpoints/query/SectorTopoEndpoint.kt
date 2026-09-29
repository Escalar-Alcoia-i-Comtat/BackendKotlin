package server.endpoints.query

import ServerDatabase
import database.entity.Sector
import io.ktor.server.plugins.ParameterConversionException
import io.ktor.server.routing.RoutingContext
import io.ktor.server.util.getValue
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.response.query.TopoResponseData
import server.response.respondFailure
import server.response.respondSuccess

/**
 * Returns the topo of a sector and the UUID of its background, so the editor can modify it.
 * Responds [Errors.ObjectNotFound] if the sector doesn't exist or doesn't have a topo.
 */
object SectorTopoEndpoint : SecureEndpointBase("/sector/{sectorId}/topo") {
    override suspend fun RoutingContext.endpoint() {
        val sectorId = try {
            val sectorId: Int by call.parameters
            sectorId
        } catch (_: ParameterConversionException) {
            return respondFailure(Errors.InvalidData)
        }

        val data = ServerDatabase.instance.query {
            val sector = Sector.findById(sectorId) ?: return@query null
            val topo = sector.topo ?: return@query null
            val background = runCatching { sector.topoImage }.getOrNull() ?: return@query null
            TopoResponseData(topo, background.nameWithoutExtension)
        } ?: return respondFailure(Errors.ObjectNotFound)

        respondSuccess(data)
    }
}
