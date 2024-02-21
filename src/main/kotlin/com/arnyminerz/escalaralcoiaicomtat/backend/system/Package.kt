package com.arnyminerz.escalaralcoiaicomtat.backend.system

object Package {
    /**
     * Gets the contents of the version file.
     * @return The version string.
     * @throws IllegalStateException If the version file is not found.
     */
    fun getVersion() {
        return this::class.java.getResourceAsStream("/version.txt").use { input ->
            if (input == null) {
                throw IllegalStateException("Version file not found")
            }
            input.bufferedReader().readText()
        }
    }
}
