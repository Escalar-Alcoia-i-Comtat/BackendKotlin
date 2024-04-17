package system

/**
 * A class that provides the names of all the environment variables used by the server.
 */
object EnvironmentVariables {
    data object Testing : EnvironmentVariable("TESTING")

    object Authentication {
        data object AuthToken : EnvironmentVariable("AUTH_TOKEN")
    }

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

    object Diagnostics {
        /**
         * The DSN for reporting crashes and performance issues to Sentry.
         */
        data object SentryDsn: EnvironmentVariable("SENTRY_DSN")
    }

    object Environment {
        /**
         * States whether the system is running in production mode.
         */
        data object IsProduction : EnvironmentVariable("IS_PRODUCTION", "false")
    }

    object Services {
        object GoogleCredentials {
            /**
             * The path to the service account file for Google Cloud services.
             */
            data object ServiceAccountFile : EnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS")
        }
    }

    object Localization {
        data object CrowdinToken : EnvironmentVariable("CROWDIN_TOKEN")
        data object CrowdinOrganization : EnvironmentVariable("CROWDIN_ORGANIZATION")
        data object CrowdinProjectId : EnvironmentVariable("CROWDIN_PROJECT_ID")
    }

    object Legacy {
        data object Importer : EnvironmentVariable("ENABLE_IMPORTER")
    }
}
