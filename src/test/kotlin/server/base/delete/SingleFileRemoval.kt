package server.base.delete

import database.entity.BaseEntity
import java.io.File

class SingleFileRemoval<EntityType : BaseEntity>(
    val fileAccessor: (EntityType) -> File?
) : FileRemoval<EntityType> {
    private val entities = mutableMapOf<Int, File?>()

    override fun exists(entity: EntityType): Boolean {
        val file = entities.getOrPut(entity.id.value) { fileAccessor(entity) }
        return file?.exists() != false
    }
}
