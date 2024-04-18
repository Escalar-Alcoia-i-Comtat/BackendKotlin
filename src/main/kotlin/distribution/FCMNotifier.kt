package distribution

import Logger
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import database.EntityTypes
import java.io.File
import system.EnvironmentVariables

/**
 * Uses FCM to notify all devices of updates to the database.
 */
internal object FCMNotifier : Notifier {
    private const val DATA_TYPE = "type"
    private const val DATA_ID = "id"

    private lateinit var app: FirebaseApp

    private val fcm: FirebaseMessaging by lazy { FirebaseMessaging.getInstance(app) }

    override fun initialize() {
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

    /**
     * Sends a notification through FCM to all devices subscribed to the given topic.
     * This method is only visible for internal calls or testing purposes.
     */
    override fun notify(topic: String, type: EntityTypes, id: Int) {
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
}
