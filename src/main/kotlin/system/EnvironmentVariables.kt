package system

import KoverIgnore

/**
 * A class that provides the names of all the environment variables used by the server.
 */
@KoverIgnore
object EnvironmentVariables {
    @KoverIgnore
    data object Testing : EnvironmentVariable("TESTING")

    @KoverIgnore
    object Authentication {
        @KoverIgnore
        data object AuthToken : EnvironmentVariable("AUTH_TOKEN")
    }

    @KoverIgnore
    object Database {
        /**
         * This variable represents the URL of the database connection.
         */
        @KoverIgnore
        data object Url : EnvironmentVariable("DATABASE_URL")

        /**
         * The driver used for connecting to the database.
         *
         * This variable represents the database driver that is used for establishing a connection to the database.
         *
         * @property Driver The driver value is a string that represents the name or identifier of the database driver.
         */
        @KoverIgnore
        data object Driver : EnvironmentVariable("DATABASE_DRIVER")

        /**
         * Represents the username for the database connection.
         */
        @KoverIgnore
        data object Username : EnvironmentVariable("DATABASE_USERNAME")

        /**
         * The password for accessing the database.
         */
        @KoverIgnore
        data object Password : EnvironmentVariable("DATABASE_PASSWORD")
    }

    @KoverIgnore
    object Diagnostics {
        /**
         * The DSN for reporting crashes and performance issues to Sentry.
         */
        @KoverIgnore
        data object SentryDsn: EnvironmentVariable("SENTRY_DSN")
    }

    @KoverIgnore
    object Environment {
        /**
         * States whether the system is running in production mode.
         */
        @KoverIgnore
        data object IsProduction : EnvironmentVariable("IS_PRODUCTION", "false")

        /**
         * The UUID of the server. Used for distinguishing between production and testing servers, for example.
         */
        @KoverIgnore
        data object ServerUUID : EnvironmentVariable("SERVER_UUID")
    }

    @KoverIgnore
    object Services {
        @KoverIgnore
        object GoogleCredentials {
            /**
             * The path to the service account file for Google Cloud services.
             */
            @KoverIgnore
            data object ServiceAccountFile : EnvironmentVariable(
                "GOOGLE_APPLICATION_CREDENTIALS",
                "/var/lib/escalaralcoiaicomtat/google-services.json"
            )
        }
    }

    @KoverIgnore
    object Localization {
        @KoverIgnore
        data object CrowdinToken : EnvironmentVariable("CROWDIN_TOKEN")
        @KoverIgnore
        data object CrowdinOrganization : EnvironmentVariable("CROWDIN_ORGANIZATION")
        @KoverIgnore
        data object CrowdinProjectId : EnvironmentVariable("CROWDIN_PROJECT_ID")
    }

    @KoverIgnore
    object Legacy {
        @KoverIgnore
        data object Importer : EnvironmentVariable("ENABLE_IMPORTER")
    }
}
