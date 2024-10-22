package server.plugins

import io.ktor.http.CacheControl
import io.ktor.http.HttpHeaders
import io.ktor.http.content.CachingOptions
import io.ktor.server.http.content.CachingOptions
import io.ktor.server.plugins.cachingheaders.CachingHeadersConfig
import java.time.ZonedDateTime
import server.response.FileSource
import server.response.FileUUID
import server.response.ResourceId
import server.response.ResourceType

/**
 * Configures the maximum age in seconds that file requests should be cached for.
 *
 * Default: `24 hours`
 */
private const val FILE_MAX_AGE = 24 * 60 * 60

/**
 * Configures the maximum age in seconds that data requests should be cached for.
 *
 * Default: `10 minutes`
 */
private const val DATA_MAX_AGE = 10 * 60

/**
 * Generates [CachingOptions] for caching file requests.
 *
 * @param maxAge The maximum age in seconds that file requests should be cached for.
 */
private fun cachingOptions(maxAge: Int): CachingOptions {
    return CachingOptions(
        cacheControl = CacheControl.MaxAge(maxAge),
        expires = ZonedDateTime.now().plusSeconds(maxAge.toLong())
    )
}

fun CachingHeadersConfig.configure() {
    options { call, outgoingContent ->
        val headers = call.response.headers

        val fileUUID = headers[HttpHeaders.FileUUID]
        val fileSource = headers[HttpHeaders.FileSource]
        val resourceType = headers[HttpHeaders.ResourceType]
        val resourceId = headers[HttpHeaders.ResourceId]?.toIntOrNull()

        if (fileUUID != null && fileSource != null) {
            return@options cachingOptions(FILE_MAX_AGE)
        } else if (resourceType != null && resourceId != null) {
            return@options cachingOptions(DATA_MAX_AGE)
        }

        null
    }
}
