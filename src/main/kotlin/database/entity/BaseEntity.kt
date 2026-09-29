package database.entity

import java.time.Instant
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.core.dao.id.EntityID

abstract class BaseEntity(
    id: EntityID<Int>,
): IntEntity(id) {
    abstract var timestamp: Instant
}
