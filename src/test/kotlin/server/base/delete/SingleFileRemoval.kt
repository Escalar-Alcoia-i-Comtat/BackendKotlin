package server.base.delete

import database.entity.BaseEntity
import java.io.File

class SingleFileRemoval<EntityType : BaseEntity>(
    val fileAccessor: (EntityType) -> File?
) : FileRemoval<EntityType> {
    override fun exists(entity: EntityType): Boolean {
        val file = fileAccessor(entity)
        return file?.exists() != false
    }
}
