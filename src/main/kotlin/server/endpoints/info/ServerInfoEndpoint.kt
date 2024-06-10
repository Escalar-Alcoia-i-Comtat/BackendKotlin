package server.endpoints.info

import ServerDatabase
import database.entity.info.Version
import io.ktor.server.application.ApplicationCall
import io.ktor.util.pipeline.PipelineContext
import server.endpoints.EndpointBase
import server.response.query.ServerInfoResponseData
import server.response.respondSuccess
import system.EnvironmentVariables
import system.Package

object ServerInfoEndpoint: EndpointBase("/info") {
    override suspend fun PipelineContext<Unit, ApplicationCall>.endpoint() {
        val uuid = EnvironmentVariables.Environment.ServerUUID.value!!
        val version = Package.getVersion()
        val databaseVersion = ServerDatabase.instance { Version.get() }

        respondSuccess(
            data = ServerInfoResponseData(version, uuid, databaseVersion)
        )
    }
}
