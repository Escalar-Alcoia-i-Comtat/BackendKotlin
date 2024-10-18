package server.request

import io.ktor.http.content.PartData
import io.ktor.utils.io.readBuffer
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.io.copyTo

/**
 * Saves the FileItem to a specified root directory. Creates any necessary parent directories.
 *
 * @param rootDir The root directory to save the FileItem to.
 * @param uuid The UUID for the name of the file. Defaults to a random one.
 * @param overwrite If `true`, the file will be removed before writing if exists.
 *
 * @throws IOException If there's a problem while writing to the file system.
 */
suspend fun PartData.FileItem.save(rootDir: File, uuid: UUID? = null, overwrite: Boolean = true): File {
    rootDir.mkdirs()

    val fileExtension = originalFileName?.takeLastWhile { it != '.' }
    val fileName = "${uuid ?: UUID.randomUUID()}.$fileExtension"
    val targetFile = File(rootDir, fileName)

    if (overwrite && targetFile.exists()) {
        targetFile.delete()
    }

    provider().readBuffer().use { input ->
        targetFile.outputStream().use { output ->
            input.copyTo(output)
        }
    }

    return targetFile
}
