package server.base.delete

import database.entity.BaseEntity

interface FileRemoval<EntityType: BaseEntity> {
    fun exists(entity: EntityType): Boolean
}
