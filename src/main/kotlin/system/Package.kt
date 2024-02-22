package system

object Package {
    /**
     * Gets the contents of the version file.
     * @return The version string.
     * @throws IllegalStateException If the version file is not found.
     */
    fun getVersion(): String {
        val version: String = this::class.java.getResourceAsStream("/version.txt").use { input ->
            checkNotNull(input) { "Version file not found" }
            input.bufferedReader().readText()
        }
        return version
    }
}
