package server.error

import database.table.Paths
import io.ktor.http.HttpStatusCode

/**
 * Singleton class representing common error types.
 */
@Suppress("MagicNumber")
object Errors {
    val EndpointNotFound = Error(1, "The target endpoint was not found.", HttpStatusCode.NotFound)
    val ObjectNotFound = Error(2, "The requested data was not found.", HttpStatusCode.Gone)
    val FileNotFound = Error(3, "The requested file was not found.", HttpStatusCode.Gone)
    val ParentNotFound = Error(4, "The parent element was not found.", HttpStatusCode.Gone)

    val MissingData = Error(10, "The request misses some required data.", HttpStatusCode.BadRequest)
    val InvalidData = Error(11, "The request has some data of the wrong type.", HttpStatusCode.BadRequest)
    val TooManyImages = Error(12, "Too many images added. Maximum: ${Paths.MAX_IMAGES}", HttpStatusCode.BadRequest)

    val Conflict = Error(20, "Multiple parameters in the request conflict.", HttpStatusCode.Conflict)

    val AuthenticationDisabled = Error(30, "The server has disabled secure endpoints", HttpStatusCode.NotImplemented)
    val AuthenticationRequired = Error(31, "Authentication token is required", HttpStatusCode.Unauthorized)
    val AuthenticationInvalid = Error(32, "Authentication requires a Bearer token", HttpStatusCode.MethodNotAllowed)
    val AuthenticationFailed = Error(33, "Authentication token is not valid", HttpStatusCode.Forbidden)

    val DatabaseNotEmpty = Error(40, "Database must be empty", HttpStatusCode.PreconditionFailed)
    val NotRunning = Error(41, "Not running", HttpStatusCode.PreconditionFailed)

    val CouldNotClear = Error(50, "Could not clear an invalid request", HttpStatusCode.InternalServerError)
}
