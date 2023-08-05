package com.arnyminerz.escalaralcoiaicomtat.backend.system

/**
 * A class that provides the names of all the environment variables used by the server.
 */
object EnvironmentVariables {
    data object Testing : EnvironmentVariable("TESTING")

    object Database {
        /**
         * This variable represents the URL of the database connection.
         */
        data object Url : EnvironmentVariable("DATABASE_URL")

        /**
         * The driver used for connecting to the database.
         *
         * This variable represents the database driver that is used for establishing a connection to the database.
         *
         * @property Driver The driver value is a string that represents the name or identifier of the database driver.
         */
        data object Driver : EnvironmentVariable("DATABASE_DRIVER")

        /**
         * Represents the username for the database connection.
         */
        data object Username : EnvironmentVariable("DATABASE_USERNAME")

        /**
         * The password for accessing the database.
         */
        data object Password : EnvironmentVariable("DATABASE_PASSWORD")
    }

    object Authentication {
        data object AuthToken : EnvironmentVariable("AUTH_TOKEN")
    }

    object Legacy {
        data object Importer : EnvironmentVariable("ENABLE_IMPORTER")
    }
}
