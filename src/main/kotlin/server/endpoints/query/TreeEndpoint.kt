package server.endpoints.query

import ServerDatabase
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import database.table.Areas
import database.table.Paths
import database.table.Sectors
import database.table.Zones
import io.ktor.http.HttpHeaders
import io.ktor.server.response.header
import io.ktor.server.routing.RoutingContext
import org.jetbrains.exposed.sql.SortOrder
import server.endpoints.EndpointBase
import server.response.query.TreeResponseData
import server.response.respondSuccess

object TreeEndpoint : EndpointBase("/tree") {
    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun RoutingContext.endpoint() {
        val lastUpdatedArea = ServerDatabase {
            Area.all().orderBy(Areas.timestamp to SortOrder.DESC).limit(1).firstOrNull()
        }
        val lastUpdatedZone = ServerDatabase {
            Zone.all().orderBy(Zones.timestamp to SortOrder.DESC).limit(1).firstOrNull()
        }
        val lastUpdatedSector = ServerDatabase {
            Sector.all().orderBy(Sectors.timestamp to SortOrder.DESC).limit(1).firstOrNull()
        }
        val lastUpdatedPath = ServerDatabase {
            Path.all().orderBy(Paths.timestamp to SortOrder.DESC).limit(1).firstOrNull()
        }
        val lastUpdate = listOf(lastUpdatedArea, lastUpdatedZone, lastUpdatedSector, lastUpdatedPath)
            .maxByOrNull { it?.timestamp?.epochSecond ?: 0 }

        if (lastUpdate != null) {
            call.response.header(HttpHeaders.LastModified, lastUpdate.timestamp)
            call.response.header(HttpHeaders.ETag, ServerDatabase { lastUpdate.hashCode().toHexString() })
        }

        val array = ServerDatabase {
            val zones = Zone.all().toList()
            val sectors = Sector.all().toList()
            val paths = Path.all().toList()

            Area.all().onEach { it.populateZones(zones, sectors, paths) }
                .toList()
        }
        respondSuccess(
            data = TreeResponseData(array.toList())
        )
    }
}
