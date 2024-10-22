package storage

import java.io.File

enum class FileType(val headerValue: String, val fetcher: (uuid: String) -> File?) {
    IMAGE("Images", Storage::imageFile),
    TRACK("Tracks", Storage::trackFile)
}
