package diagnostics

import io.sentry.Sentry
import system.EnvironmentVariables

object Diagnostics {
    var isInitialized: Boolean = false
        private set

    /**
     * Configures Sentry if the environment is correctly configured (environment variables set).
     *
     * @return `true` if Sentry was configured correctly, false otherwise.
     */
    fun init(): Boolean {
        return EnvironmentVariables.Diagnostics.SentryDsn.value?.let { dsn ->
            Sentry.init { options ->
                options.dsn = dsn
                options.tracesSampleRate = 1.0
                options.isDebug = EnvironmentVariables.Environment.IsProduction.value != "true"
            }
            true
        } ?: false
    }
}
