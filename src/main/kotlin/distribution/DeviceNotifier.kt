package distribution

import Logger
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import database.EntityTypes
import java.io.File
import org.jetbrains.annotations.VisibleForTesting
import system.EnvironmentVariables

/**
 * Uses FCM to notify all devices of updates to the database.
 */
object DeviceNotifier {
    private const val TOPIC_CREATED = "created"
    private const val TOPIC_UPDATED = "updated"
    private const val TOPIC_DELETED = "deleted"

    private const val DATA_TYPE = "type"
    private const val DATA_ID = "id"

    private lateinit var app: FirebaseApp

    private val fcm: FirebaseMessaging by lazy { FirebaseMessaging.getInstance(app) }

    fun initialize() {
        val saf = EnvironmentVariables.Services.GoogleCredentials.ServiceAccountFile
        if (!saf.isSet) {
            Logger.info("Google service account file not set. Device notifications will not be sent.")
            return
        }
        val serviceAccountFile = File(saf.value!!)

        Logger.debug("Initializing Firebase with service account file: $serviceAccountFile")
        val options = FirebaseOptions.builder()
            .setCredentials(
                serviceAccountFile.inputStream().use { GoogleCredentials.fromStream(it) }
            )
            .build()

        app = FirebaseApp.initializeApp(options)
        Logger.info("Firebase initialized. Device notifications will be sent.")
    }

    @VisibleForTesting
    var notify: (topic: String, type: EntityTypes, id: Int) -> Unit = { topic, type, id ->
        // Run only if the app is initialized
        if (this::app.isInitialized) {
            val message = Message.builder()
                .setTopic(topic)
                .putData(DATA_TYPE, type.name)
                .putData(DATA_ID, id.toString())
                .build()
            fcm.send(message)
        }
    }

    /**
     * Notifies all devices that a new entity has been created.
     *
     * If [initialize] has not been called, or if the service account file is not set, this method will do nothing.
     *
     * @param type The type of entity that was created.
     * @param id The ID of the entity that was created.
     */
    fun notifyCreated(type: EntityTypes, id: Int) = notify(TOPIC_CREATED, type, id)

    /**
     * Notifies all devices that an entity has been updated.
     *
     * If [initialize] has not been called, or if the service account file is not set, this method will do nothing.
     *
     * @param type The type of entity that was created.
     * @param id The ID of the entity that was created.
     */
    fun notifyUpdated(type: EntityTypes, id: Int) = notify(TOPIC_UPDATED, type, id)

    /**
     * Notifies all devices that an entity has been deleted.
     *
     * If [initialize] has not been called, or if the service account file is not set, this method will do nothing.
     *
     * @param type The type of entity that was created.
     * @param id The ID of the entity that was created.
     */
    fun notifyDeleted(type: EntityTypes, id: Int) = notify(TOPIC_DELETED, type, id)
}
