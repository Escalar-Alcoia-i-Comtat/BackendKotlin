package com.arnyminerz.escalaralcoiaicomtat.backend.server.error

import io.ktor.http.HttpStatusCode

/**
 * Singleton class representing common error types.
 */
@Suppress("MagicNumber")
object Errors {
    val EndpointNotFound = Error(1, "The target endpoint was not found.", HttpStatusCode.NotFound)
    val ObjectNotFound = Error(2, "The requested data was not found.", HttpStatusCode.Gone)
    val FileNotFound = Error(3, "The requested file was not found.", HttpStatusCode.Gone)

    val MissingData = Error(10, "The request misses some required data.", HttpStatusCode.BadRequest)
    val InvalidData = Error(11, "The request has some data of the wrong type.", HttpStatusCode.BadRequest)

    val Conflict = Error(20, "Multiple parameters in the request conflict.", HttpStatusCode.Conflict)
}
