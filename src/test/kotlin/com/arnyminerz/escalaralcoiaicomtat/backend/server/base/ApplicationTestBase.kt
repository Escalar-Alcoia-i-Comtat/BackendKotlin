package com.arnyminerz.escalaralcoiaicomtat.backend.server.base

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import setupApplication

/**
 * Provides some utility functions to perform operations in the application.
 */
abstract class ApplicationTestBase {
    /**
     * Prepares the testing database, and configures the applications to start making requests and testing application
     * endpoints. Perform all the desired steps in [block].
     */
    fun test(block: suspend ApplicationTestBuilder.() -> Unit) {
        // Configure database
        ServerDatabase.url = "jdbc:sqlite:testing.db"
        File("testing.db").takeIf { it.exists() }?.delete()

        // Access the database once to initialize
        ServerDatabase.instance

        // Configure storage
        Storage.BaseDir = File(System.getProperty("user.home"), ".EAIC-Testing")

        testApplication {
            application {
                setupApplication()
            }
            block()
        }

        // Delete all files created
        Storage.BaseDir.deleteRecursively()
    }
}
