package server.base

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import database.EntityTypes
import database.entity.BaseEntity
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.base.ApplicationTestBase.Companion.AUTH_TOKEN
import server.base.delete.FileRemoval
import server.error.Errors

fun <EntityType: BaseEntity> ApplicationTestBase.testDeleting(
    type: EntityTypes<EntityType>,
    fileRemovals: List<FileRemoval<EntityType>> = emptyList()
) = test {
    val elementId = type.provide()
    assertNotNull(elementId)

    val element = ServerDatabase.instance.query { type.getter(elementId) }

    val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

    for (removal in fileRemovals) {
        assertTrue { removal.exists(element) }
    }

    client.delete("/${type.urlName}/$elementId") {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
    }.apply {
        assertSuccess()
    }

    for (removal in fileRemovals) {
        assertFalse { removal.exists(element) }
    }

    ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

    client.get("/${type.urlName}/$elementId") {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
    }.apply {
        assertFailure(Errors.ObjectNotFound)
    }

    assertNotificationSent(Notifier.TOPIC_DELETED, type, elementId)
}

fun <EntityType: BaseEntity> ApplicationTestBase.testDeletingNotFound(
    type: EntityTypes<EntityType>
) = test {
    client.delete("/${type.urlName}/123") {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
    }.apply {
        assertFailure(Errors.ObjectNotFound)
    }
    assertNotificationNotSent(Notifier.TOPIC_DELETED, EntityTypes.SECTOR)
}
