package server.base

import Logger
import ServerDatabase
import database.EntityTypes
import distribution.Notifier
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.jetbrains.exposed.sql.StdOutSqlLogger
import setupApplication
import storage.Storage
import system.EnvironmentVariables

/**
 * Provides some utility functions to perform operations in the application.
 */
abstract class ApplicationTestBase {
    companion object {
        const val AUTH_TOKEN = "password"
    }

    object DeviceNotifier : Notifier {
        class Notification(
            val topic: String,
            val type: EntityTypes,
            val id: Int
        )

        var notificationStack = emptyList<Notification>()

        override fun initialize() { /* Nothing to do */ }

        override fun notify(topic: String, type: EntityTypes, id: Int) {
            Logger.info("Received notification on topic $topic for entity $type with id $id.")
            notificationStack = notificationStack.toMutableList().plusElement(
                Notification(topic, type, id)
            )
        }
    }

    /**
     * Asserts that a notification was sent with the given parameters.
     * The last notification is cleared after this method is called.
     * @param topic The topic of the notification.
     * @param type The type of the entity that was notified.
     * @param id The id of the entity that was notified.
     */
    fun assertNotificationSent(topic: String, type: EntityTypes, id: Int) {
        var noti: DeviceNotifier.Notification? = null
        try {
            DeviceNotifier.notificationStack.find { notification ->
                notification.topic == topic && notification.type == type && notification.id == id
            }.also {
                noti = it
                assertNotNull(it, "Notification was not sent")
            }
        } finally {
            noti?.let {
                val list = DeviceNotifier.notificationStack.toMutableList()
                assertTrue { list.remove(it) }
                DeviceNotifier.notificationStack = list
            }
        }
    }

    /**
     * Asserts that no notification was sent.
     */
    fun assertNotificationNotSent(topic: String, type: EntityTypes) {
        var noti: DeviceNotifier.Notification? = null
        try {
            DeviceNotifier.notificationStack.find { notification ->
                notification.topic == topic && notification.type == type
            }.also {
                noti = it
                assertNull(it, "Notification was sent")
            }
        } finally {
            // If it was indeed sent, remove it from the stack
            noti?.let {
                val list = DeviceNotifier.notificationStack.toMutableList()
                assertTrue { list.remove(it) }
                DeviceNotifier.notificationStack = list
            }
        }
    }

    @BeforeTest
    fun mockFCM() {
        Notifier.setInstance(DeviceNotifier)
    }

    @AfterTest
    fun clearNotificationStack() {
        DeviceNotifier.notificationStack = emptyList()
    }

    @BeforeTest
    fun setUUID() {
        EnvironmentVariables.Environment.ServerUUID.value = "d0598a84-ec10-4666-b83e-611881c49546"
    }

    /**
     * Prepares the testing database, and configures the applications to start making requests and testing application
     * endpoints. Perform all the desired steps in [block].
     */
    fun test(block: suspend ApplicationTestBuilder.() -> Unit) {
        // Configure database
        ServerDatabase.url = "jdbc:sqlite:testing.db"
        ServerDatabase.logger = StdOutSqlLogger
        File("testing.db").takeIf { it.exists() }?.delete()

        // Access the database once to initialize
        ServerDatabase.instance.initialize()

        // Configure storage
        Storage.BaseDir = File(System.getProperty("user.home"), ".EAIC-Testing")

        // Configure authentication
        EnvironmentVariables.Authentication.AuthToken.value = AUTH_TOKEN

        testApplication {
            application {
                setupApplication()
            }
            block()
        }

        // Delete all files created
        Storage.BaseDir.deleteRecursively()
    }

    suspend fun ApplicationTestBuilder.get(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.get(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }

    suspend fun ApplicationTestBuilder.post(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.post(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }

    suspend fun ApplicationTestBuilder.patch(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.patch(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }

    suspend fun ApplicationTestBuilder.delete(
        urlString: String,
        block: HttpRequestBuilder.() -> Unit = {}
    ) = client.delete(urlString) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        block()
    }
}
