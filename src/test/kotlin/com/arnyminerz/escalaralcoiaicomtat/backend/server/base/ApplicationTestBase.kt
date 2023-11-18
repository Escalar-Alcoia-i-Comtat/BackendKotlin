package com.arnyminerz.escalaralcoiaicomtat.backend.server.base

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import org.jetbrains.exposed.sql.StdOutSqlLogger
import setupApplication

/**
 * Provides some utility functions to perform operations in the application.
 */
abstract class ApplicationTestBase {
    companion object {
        const val AUTH_TOKEN = "password"
    }

    /**
     * Prepares the testing database, and configures the applications to start making requests and testing application
     * endpoints. Perform all the desired steps in [block].
     */
    fun test(block: suspend ApplicationTestBuilder.() -> Unit) {
        // Configure database
        ServerDatabase.url = "jdbc:sqlite:testing.db"
        ServerDatabase.logger = StdOutSqlLogger
        File("testing.db").takeIf { it.exists() }?.delete()

        // Access the database once to initialize
        ServerDatabase.instance.initialize()

        // Configure storage
        Storage.BaseDir = File(System.getProperty("user.home"), ".EAIC-Testing")

        // Configure authentication
        EnvironmentVariables.Authentication.AuthToken.value = AUTH_TOKEN

        testApplication {
            application {
                setupApplication()
            }
            block()
        }

        // Delete all files created
        Storage.BaseDir.deleteRecursively()
    }

    suspend fun ApplicationTestBuilder.get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.get(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }

    suspend fun ApplicationTestBuilder.post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.post(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }

    suspend fun ApplicationTestBuilder.patch(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.patch(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }

    suspend fun ApplicationTestBuilder.delete(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.delete(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }
}
