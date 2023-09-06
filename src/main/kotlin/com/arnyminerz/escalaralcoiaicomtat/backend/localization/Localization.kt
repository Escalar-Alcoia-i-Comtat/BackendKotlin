package com.arnyminerz.escalaralcoiaicomtat.backend.localization

import com.arnyminerz.escalaralcoiaicomtat.backend.Logger
import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Path
import com.arnyminerz.escalaralcoiaicomtat.backend.system.EnvironmentVariables
import com.crowdin.client.Client
import com.crowdin.client.core.http.exceptions.HttpBadRequestException
import com.crowdin.client.core.http.exceptions.HttpException
import com.crowdin.client.core.model.Credentials
import com.crowdin.client.core.model.PatchOperation
import com.crowdin.client.core.model.PatchRequest
import com.crowdin.client.sourcefiles.model.AddBranchRequest
import com.crowdin.client.sourcefiles.model.AddDirectoryRequest
import com.crowdin.client.sourcefiles.model.AddFileRequest
import com.crowdin.client.sourcefiles.model.Branch
import com.crowdin.client.sourcefiles.model.Directory
import com.crowdin.client.sourcefiles.model.FileInfo
import com.crowdin.client.sourcestrings.model.AddSourceStringRequest
import com.crowdin.client.sourcestrings.model.SourceString
import org.json.JSONObject

object Localization {
    private const val BRANCH_NAME = "backend"
    private const val BRANCH_TITLE = "Backend Localization"

    private const val DIRECTORY_NAME = "database"
    private const val DIRECTORY_TITLE = "Database"

    private const val DESCRIPTIONS_FILE_NAME = "path_descriptions"
    private const val DESCRIPTIONS_FILE_TITLE = "Path Descriptions"

    @Volatile
    private var client: Client? = null

    @Volatile
    private var projectId: Long? = null

    @Volatile
    private var pathDescriptionsFile: FileInfo? = null

    /**
     * Initializes the Crowdin localization service.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     */
    @Synchronized
    fun init() {
        val token = EnvironmentVariables.Localization.CrowdinToken.value
        val organization = EnvironmentVariables.Localization.CrowdinOrganization.value
        projectId = EnvironmentVariables.Localization.CrowdinProjectId.value?.toLongOrNull()

        if (token == null || projectId == null) {
            Logger.warn("Won't enable Crowdin integration: Environment variables not set")
            return
        }
        Logger.info("Initializing Crowdin integration...")
        Logger.debug("Crowdin organization: $organization")
        Logger.debug("Crowdin Project ID: $projectId")

        val credentials = Credentials(token, organization)
        client = Client(credentials)

        val branch: Branch = getBranch()
        val directory: Directory = getDirectory(branch)

        pathDescriptionsFile = getFile(directory, DESCRIPTIONS_FILE_NAME, DESCRIPTIONS_FILE_TITLE)

        Logger.info("Crowdin is ready.")
    }

    suspend fun synchronizePathDescriptions() {
        val pathDescriptionsFile = pathDescriptionsFile
        if (pathDescriptionsFile == null) {
            Logger.debug("Won't synchronize path descriptions with Crowdin: Not initialized")
            return
        }

        Logger.info("Synchronizing path descriptions with Crowdin...")
        val paths = ServerDatabase.instance.query {
            Path.all().filter { it.description != null }
        }
        if (paths.isEmpty()) {
            Logger.info("There isn't any path with a description.")
        } else {
            paths.forEach { path ->
                getSourceString(
                    pathDescriptionsFile,
                    "path_${path.id.value}",
                    path.description ?: "",
                    "Description for path ${path.id} (${path.displayName})"
                )
            }
        }
    }

    /**
     * Synchronizes the path description with Crowdin.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     */
    suspend fun synchronizePathDescription(path: Path) {
        val pathDescriptionsFile = pathDescriptionsFile
        if (pathDescriptionsFile == null) {
            Logger.debug("Won't synchronize path descriptions with Crowdin: Not initialized")
            return
        }

        ServerDatabase.instance.query {
            getSourceString(
                pathDescriptionsFile,
                "path_${path.id.value}",
                path.description ?: "",
                "Description for path ${path.id} (${path.displayName})"
            )
        }
    }

    /**
     * Deletes the given path's description from Crowdin.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     */
    suspend fun deletePathDescription(path: Path) {
        val pathDescriptionsFile = pathDescriptionsFile
        if (pathDescriptionsFile == null) {
            Logger.debug("Won't synchronize path descriptions with Crowdin: Not initialized")
            return
        }

        ServerDatabase.instance.query {
            deleteSourceString(pathDescriptionsFile, "path_${path.id.value}")
        }
    }

    /**
     * Fetches the localization branch for the current [projectId] using [client].
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     * @throws IllegalStateException If [client] or [projectId] is null
     */
    private fun getBranch(): Branch {
        val sourceFilesApi = client?.sourceFilesApi

        check(sourceFilesApi != null) { "Client has not been initialized." }
        check(projectId != null) { "projectId has not been initialized." }

        val branches = sourceFilesApi.listBranches(projectId, BRANCH_NAME, 1, 0).data
        return if (branches.isEmpty()) {
            Logger.info("Crowdin branch not initialized. Creating...")

            sourceFilesApi.addBranch(
                projectId,
                AddBranchRequest().apply {
                    name = BRANCH_NAME
                    title = BRANCH_TITLE
                }
            ).data
        } else {
            Logger.debug("Crowdin branch already initialized.")

            branches.first().data
        }
    }

    /**
     * Fetches the localization branch for the current [projectId] using [client].
     *
     * @param branch The branch where the directory is placed at.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     * @throws IllegalStateException If [client] or [projectId] is null
     */
    private fun getDirectory(branch: Branch): Directory {
        val sourceFilesApi = client?.sourceFilesApi

        check(sourceFilesApi != null) { "Client has not been initialized." }
        check(projectId != null) { "projectId has not been initialized." }

        val directories = sourceFilesApi.listDirectories(
            projectId,
            branch.id,
            null,
            DIRECTORY_NAME,
            null,
            1,
            0
        ).data
        return if (directories.isEmpty()) {
            Logger.info("Crowdin directory not initialized. Creating...")

            sourceFilesApi.addDirectory(
                projectId,
                AddDirectoryRequest().apply {
                    branchId = branch.id
                    name = DIRECTORY_NAME
                    title = DIRECTORY_TITLE
                }
            ).data
        } else {
            Logger.debug("Crowdin directory already initialized.")

            directories.first().data
        }
    }

    /**
     * Fetches a file from Crowdin.
     *
     * @param directory The directory where the file is located at.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     * @throws IllegalStateException If [client] or [projectId] is null
     */
    @Suppress("SameParameterValue")
    private fun getFile(directory: Directory, name: String, title: String): FileInfo {
        val sourceFilesApi = client?.sourceFilesApi
        val storageApi = client?.storageApi

        check(sourceFilesApi != null) { "Client has not been initialized." }
        check(storageApi != null) { "Client has not been initialized." }
        check(projectId != null) { "projectId has not been initialized." }

        val descriptionsFiles = sourceFilesApi.listFiles(
            projectId,
            null,
            directory.id,
            DESCRIPTIONS_FILE_NAME,
            null,
            1,
            0
        ).data
        return if (descriptionsFiles.isEmpty()) {
            Logger.info("Crowdin descriptions file not initialized. Creating storage...")

            val storage = storageApi.addStorage("$name.json", JSONObject().toString()).data

            Logger.info("  Storage ready. Creating file...")
            sourceFilesApi.addFile(
                projectId,
                AddFileRequest().apply {
                    this.storageId = storage.id
                    this.directoryId = directory.id
                    this.name = "$name.json"
                    this.title = title
                    this.type = "json"
                }
            ).data
        } else {
            Logger.debug("Crowdin descriptions file already initialized.")

            descriptionsFiles.first().data
        }
    }

    /**
     * Gets or adds a source string to the given file.
     *
     * @param fileInfo The information of the file where the string will be added at. See [getFile].
     * @param identifier Defines unique string identifier.
     * @param text Text for translation.
     * @param context Use to provide additional information for better source text understanding.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     * @throws IllegalStateException If [client] or [projectId] is null
     *
     * @return The requested [SourceString].
     */
    private fun getSourceString(fileInfo: FileInfo, identifier: String, text: String, context: String?): SourceString {
        val sourceStringsApi = client?.sourceStringsApi

        check(sourceStringsApi != null) { "Client has not been initialized." }
        check(projectId != null) { "projectId has not been initialized." }

        val sourceStrings = sourceStringsApi.listSourceStrings(
            projectId,
            fileInfo.id,
            0,
            null,
            null,
            null,
            identifier,
            "identifier",
            1,
            0
        ).data

        return if (sourceStrings.isEmpty()) {
            Logger.info("Creating source string ID#$identifier")

            sourceStringsApi.addSourceString(
                projectId,
                AddSourceStringRequest().apply {
                    this.fileId = fileInfo.id
                    this.identifier = identifier
                    this.context = context
                    this.text = text
                }
            ).data
        } else {
            sourceStrings.first().data.also { sourceString ->
                val patchRequests = mutableListOf<PatchRequest>()
                if (sourceString.text != text) {
                    patchRequests += PatchRequest().apply {
                        op = PatchOperation.REPLACE
                        path = "/text"
                        value = text
                    }
                }
                if (sourceString.context != context) {
                    patchRequests += PatchRequest().apply {
                        op = PatchOperation.REPLACE
                        path = "/context"
                        value = context
                    }
                }
                if (patchRequests.isNotEmpty()) {
                    Logger.info("Source string ID#$identifier has been updated. Notifying Crowdin...")
                    sourceStringsApi.editSourceString(projectId, sourceString.id, patchRequests)
                } else {
                    Logger.debug("Source string ID#$identifier already up to date.")
                }
            }
        }
    }

    /**
     * Deletes the source string that matches the given [identifier]. Does nothing if it doesn't exist.
     *
     * @param fileInfo The information of the file where the string will be added at. See [getFile].
     * @param identifier Defines unique string identifier.
     *
     * @throws HttpException If a request to the Crowdin API fails.
     * @throws HttpBadRequestException If a request to the Crowdin API was badly formatted.
     * @throws IllegalStateException If [client] or [projectId] is null
     */
    private fun deleteSourceString(fileInfo: FileInfo, identifier: String) {
        val sourceStringsApi = client?.sourceStringsApi

        check(sourceStringsApi != null) { "Client has not been initialized." }
        check(projectId != null) { "projectId has not been initialized." }

        val sourceString = sourceStringsApi.listSourceStrings(
            projectId,
            fileInfo.id,
            0,
            null,
            null,
            null,
            identifier,
            "identifier",
            1,
            0
        ).data.firstOrNull()?.data

        if (sourceString == null) {
            Logger.debug("Source string ID#$identifier not found. Cannot delete.")
        } else {
            Logger.info("Deleting source string ID#$identifier...")
            sourceStringsApi.deleteSourceString(projectId, sourceString.id)
        }
    }
}
