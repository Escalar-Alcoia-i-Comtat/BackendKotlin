package server.base

import ServerDatabase
import assertions.assertSuccess
import database.EntityTypes
import database.entity.BaseEntity
import database.entity.info.LastUpdate
import database.serialization.Json
import distribution.Notifier
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import server.base.ApplicationTestBase.Companion.AUTH_TOKEN
import server.base.patch.PropertyValuePair
import server.response.files.RequestFilesResponseData
import storage.HashUtils
import storage.MessageDigestAlgorithm
import storage.Storage
import java.io.File
import java.security.MessageDigest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

fun <EntityType: BaseEntity, PropertyType: Any> ApplicationTestBase.testPatching(
    type: EntityTypes<EntityType>,
    propertyName: String,
    propertyValue: PropertyType?,
    propertyAccessor: (EntityType) -> PropertyType?
) = testPatching(type, listOf(PropertyValuePair(propertyName, propertyValue, propertyAccessor)))

fun <EntityType: BaseEntity, PropertyType: Any> ApplicationTestBase.testPatching(
    type: EntityTypes<EntityType>,
    properties: List<PropertyValuePair<EntityType, PropertyType>>
) = test {
    val elementId = type.provide(this)
    assertNotNull(elementId)

    val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }
    val oldTimestamp = ServerDatabase.instance.query { type.getter(elementId).timestamp }

    client.submitFormWithBinaryData(
        url = "/${type.urlName}/$elementId",
        formData = formData {
            for (property in properties) {
                val newValue = property.newValue
                val propertyName = property.propertyName

                when (newValue) {
                    null -> append(propertyName, "\u0000")
                    is Number -> append(propertyName, newValue)
                    is Iterable<*> -> if (newValue.firstOrNull() is Serializable)
                        append(propertyName, Json.encodeToString(newValue))
                    else
                        append(propertyName, "[]")

                    else -> append(propertyName, newValue.toString())
                }
            }
        }
    ) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
    }.apply {
        assertSuccess()
    }

    ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

    ServerDatabase.instance.query {
        val element: EntityType = type.getter(elementId)
        assertNotNull(element)
        for (property in properties) {
            val propertyValue = property.propertyValue(element)
            val value = property.propertyValue(element)
            assertEquals(propertyValue, value)
        }
        assertNotEquals(oldTimestamp, element.timestamp)
    }

    assertNotificationSent(Notifier.TOPIC_UPDATED, type, elementId)
}

/**
 * Test updating the file of an entity.
 * @param type The type of the entity.
 * @param propertyName The name of the property that holds the file. This is the name in the multipart form.
 * @param fileMimeType The MIME type of the file (e.g., image/jpeg).
 * @param fileExtension The extension of the file (e.g., jpg).
 * @param resourcePath The path to the resource file to upload. If null, the request will be made to remove the file.
 * @param rootDir The root directory of the storage ([Storage.ImagesDir], [Storage.TracksDir]).
 * @param fileAccessor A function that returns the file of the entity. May only return null if [resourcePath] is null.
 */
@Suppress("LongParameterList")
fun <Type: BaseEntity> ApplicationTestBase.testPatchingFile(
    type: EntityTypes<Type>,
    propertyName: String,
    fileMimeType: String,
    fileExtension: String,
    resourcePath: String?,
    rootDir: File,
    fileAccessor: (Type) -> File?
) = test {
    val elementId = type.provide(this)
    assertNotNull(elementId)

    val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }
    val oldTimestamp = ServerDatabase.instance.query { type.getter(elementId).timestamp }

    val fileBytes = if (resourcePath != null) this::class.java.getResourceAsStream(resourcePath)!!.use {
        it.readBytes()
    } else {
        ByteArray(0)
    }

    client.submitFormWithBinaryData(
        url = "/${type.urlName}/$elementId",
        formData = formData {
            append(propertyName, fileBytes, Headers.build {
                append(HttpHeaders.ContentType, fileMimeType)
                append(HttpHeaders.ContentDisposition, "filename=${type.urlName}.${fileExtension}")
            })
        }
    ) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
    }.apply {
        assertSuccess()
    }

    ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

    var elementFile: String? = null

    ServerDatabase.instance.query {
        val element = type.getter(elementId)
        assertNotNull(element)

        val file = fileAccessor(element)
        if (resourcePath == null && file != null) {
            error("No resource path provided, but the file is not null.")
        }
        if (resourcePath != null) {
            // Removal requested, file should not be null and exist
            assertNotNull(file)
            elementFile = file.toRelativeString(rootDir)
            assertTrue(file.exists())
        }

        assertNotEquals(oldTimestamp, element.timestamp)
    }

    // Only fetch file if the request was not a removal
    if (resourcePath != null) {
        get("/files/$elementFile").apply {
            assertSuccess<RequestFilesResponseData> { response ->
                val data = response?.files?.first()
                assertNotNull(data)
                val serverHash = data.hash
                val localHash = HashUtils.getCheckSumFromStream(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    this::class.java.getResourceAsStream(resourcePath)!!
                )
                assertEquals(localHash, serverHash)
            }
        }
    }

    assertNotificationSent(Notifier.TOPIC_UPDATED, type, elementId)
}
