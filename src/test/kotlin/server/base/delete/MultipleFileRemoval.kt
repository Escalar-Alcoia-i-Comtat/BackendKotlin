package server.base.delete

import database.entity.BaseEntity
import java.io.File

class MultipleFileRemoval<EntityType : BaseEntity>(
    val fileAccessor: (EntityType) -> List<File>?
) : FileRemoval<EntityType> {
    override fun exists(entity: EntityType): Boolean {
        val files = fileAccessor(entity)
        return files?.all { file -> file.exists() } != false
    }
}
