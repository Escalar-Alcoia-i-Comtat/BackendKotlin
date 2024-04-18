package distribution

import database.EntityTypes
import org.jetbrains.annotations.VisibleForTesting

interface Notifier {
    companion object {
        const val TOPIC_CREATED = "created"
        const val TOPIC_UPDATED = "updated"
        const val TOPIC_DELETED = "deleted"

        private var instance: Notifier = FCMNotifier

        @VisibleForTesting
        fun setInstance(notifier: Notifier) { instance = notifier }

        fun getInstance(): Notifier = instance
    }

    fun initialize()

    /**
     * Sends a notification to all devices subscribed to the given topic.
     *
     * @param topic The topic to send the notification to.
     * @param type The type of entity that was created, updated, or deleted.
     * @param id The ID of the entity that was created, updated, or deleted.
     */
    fun notify(topic: String, type: EntityTypes, id: Int)

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
