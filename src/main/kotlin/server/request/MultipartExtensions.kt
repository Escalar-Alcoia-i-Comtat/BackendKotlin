package server.request

import io.ktor.http.content.PartData
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.UUID
import kotlin.io.path.exists
import kotlin.io.path.outputStream

/**
 * Saves the FileItem to a specified root directory. Creates any necessary parent directories.
 *
 * @param rootDir The root directory to save the FileItem to.
 * @param uuid The UUID for the name of the file. Defaults to a random one.
 *
 * @throws IOException If there's a problem while writing to the file system.
 */
suspend fun PartData.FileItem.save(rootDir: File, uuid: UUID? = null): File {
    rootDir.mkdirs()

    val fileExtension = originalFileName?.takeLastWhile { it != '.' }
    val fileName = "${uuid ?: UUID.randomUUID()}.$fileExtension"
    val targetFile = File(rootDir, fileName).toPath()

    if (targetFile.exists()) {
        Files.delete(targetFile)
    }
    Files.createDirectories(targetFile.parent)

    targetFile.outputStream().use { output ->
        provider().copyTo(output)
    }

    assert(targetFile.exists())

    return targetFile.toFile()
}
