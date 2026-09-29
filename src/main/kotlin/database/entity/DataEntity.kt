package database.entity

import java.net.URL
import org.jetbrains.exposed.v1.core.dao.id.EntityID

abstract class DataEntity(
    id: EntityID<Int>
): BaseEntity(id) {
    abstract var displayName: String
    abstract var webUrl: URL
}
