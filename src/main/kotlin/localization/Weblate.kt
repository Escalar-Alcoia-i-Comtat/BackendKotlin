package localization

import Logger
import database.serialization.Json
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import system.EnvironmentVariables

object Weblate {
    private val json = Json

    private lateinit var client: HttpClient

    private lateinit var projectSlug: String
    private lateinit var pathsComponent: String

    suspend fun initialize() {
        if (!EnvironmentVariables.Localization.Weblate.Url.isSet) {
            Logger.warn("Weblate URL is not set. Localization features will be disabled.")
            return
        }
        if (!EnvironmentVariables.Localization.Weblate.ProjectSlug.isSet) {
            Logger.warn("Weblate project slug is not set. Localization features will be disabled.")
            return
        }
        projectSlug = EnvironmentVariables.Localization.Weblate.ProjectSlug.value!!
        pathsComponent = EnvironmentVariables.Localization.Weblate.PathsComponentSlug.value!!

        client = HttpClient {
            install(ContentNegotiation) {
                json(json)
            }
            defaultRequest {
                url(EnvironmentVariables.Localization.Weblate.Url.value!!)
            }
        }

        // First, check if the component exists
        client.get("/api/components/$projectSlug/$pathsComponent/")
    }

    private suspend fun pathsComponentExists(): Boolean {
        val httpResponse = client.get("/api/components/$projectSlug/$pathsComponent/")
        return httpResponse.status != HttpStatusCode.NotFound
    }

    private suspend fun createPathsComponent() {
        val response = client.post("/api/projects/$projectSlug/components/") {

        }
        if (response.status != HttpStatusCode.Created) {
            throw Exception("Failed to create paths component: ${response.status}")
        }
    }
}
