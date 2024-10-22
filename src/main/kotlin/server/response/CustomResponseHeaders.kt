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

/**
 * A header included in the responses of data requests, containing the type of the data. One of:
 * - `Area`
 * - `Zone`
 * - `Sector`
 * - `Path`
 */
val HttpHeaders.ResourceType: String get() = "X-Resource-Type"

/**
 * A header included in the responses of data requests, containing the ID of the data.
 */
val HttpHeaders.ResourceId: String get() = "X-Resource-Id"
