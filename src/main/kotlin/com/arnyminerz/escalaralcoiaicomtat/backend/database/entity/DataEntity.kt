package com.arnyminerz.escalaralcoiaicomtat.backend.database.entity

import java.net.URL
import org.jetbrains.exposed.dao.id.EntityID

abstract class DataEntity(
    id: EntityID<Int>
): BaseEntity(id) {
    abstract var displayName: String
    abstract var webUrl: URL
}
