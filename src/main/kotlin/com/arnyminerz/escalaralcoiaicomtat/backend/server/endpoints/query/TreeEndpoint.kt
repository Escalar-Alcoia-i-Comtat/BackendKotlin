package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.query

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.EndpointBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.response.respondSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.mapJson
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext

object TreeEndpoint : EndpointBase() {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val array = ServerDatabase.instance.query {
            val zones = Zone.all()
            val sectors = Sector.all()
            val paths = Path.all()

            Area.all().mapJson { it.toJsonWithZones(zones, sectors, paths) }
        }
        respondSuccess(
            jsonOf("areas" to array)
        )
    }
}
