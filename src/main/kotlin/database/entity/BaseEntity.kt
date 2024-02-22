package database.entity

import java.time.Instant
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

abstract class BaseEntity(
    id: EntityID<Int>,
): IntEntity(id) {
    abstract var timestamp: Instant
}
