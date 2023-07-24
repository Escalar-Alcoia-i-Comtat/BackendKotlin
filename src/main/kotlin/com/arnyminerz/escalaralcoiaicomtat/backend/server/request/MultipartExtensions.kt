package com.arnyminerz.escalaralcoiaicomtat.backend.server.request

import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import java.io.File
import java.util.UUID

/**
 * Saves the FileItem to a specified root directory. Creates any necessary parent directories.
 *
 * @param rootDir The root directory to save the FileItem to.
 */
fun PartData.FileItem.save(rootDir: File): File {
    rootDir.mkdirs()

    val fileExtension = originalFileName?.takeLastWhile { it != '.' }
    val fileName = UUID.randomUUID().toString() + "." + fileExtension
    val targetFile = File(rootDir, fileName)

    streamProvider().buffered().use { stream ->
        targetFile.writeBytes(stream.readBytes())
    }

    return targetFile
}
