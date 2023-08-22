package com.arnyminerz.escalaralcoiaicomtat.backend.storage

import java.io.File

object Storage {
    /**
     * The base directory for the application.
     *
     * Represents the base directory of the application, which by default is the user's home directory.
     * The value is a [File] object representing the base directory.
     *
     * @see [System.getProperty]
     * @see [File]
     */
    var BaseDir: File = File("/var/lib/escalaralcoiaicomtat/files")

    val ImagesDir by lazy { File(BaseDir, "images").also { if (!it.exists()) it.mkdirs() } }
    val TracksDir by lazy { File(BaseDir, "tracks").also { if (!it.exists()) it.mkdirs() } }

    fun imageFile(path: String) = File(ImagesDir, path)

    /**
     * Finds a file based on the given UUID.
     *
     * @param uuid The UUID used to find the file.
     *
     * @return The found File object, or null if no file is found.
     */
    fun find(uuid: String): File? {
        val imageFile = ImagesDir
            .listFiles { _, name -> name.startsWith(uuid) }
            ?.firstOrNull()
        val trackFile = TracksDir
            .listFiles { _, name -> name.startsWith(uuid) }
            ?.firstOrNull()

        return imageFile ?: trackFile
    }
}
