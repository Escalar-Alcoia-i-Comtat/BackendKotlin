package server.response

import io.ktor.http.HttpHeaders
import storage.FileType

/**
 * A header included in the responses of file requests, containing the UUID of the file.
 */
val HttpHeaders.FileUUID: String get() = "X-File-UUID"

/**
 * A header included in the responses of file requests, where the file comes from. Basically one of the following:
 * - `Images` ([FileType.IMAGE])
 * - `Tracks` ([FileType.TRACK])
 * @see FileType
 */
val HttpHeaders.FileSource: String get() = "X-File-Source"
