package database.entity

import database.serialization.BlogEntrySerializer
import database.table.BlogEntriesTable
import java.time.Instant
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import server.response.ResponseData

@Serializable(with = BlogEntrySerializer::class)
class BlogEntry(id: EntityID<Int>) : BaseEntity(id), ResponseData {
    companion object : IntEntityClass<BlogEntry>(BlogEntriesTable)

    override var timestamp: Instant by BlogEntriesTable.timestamp

    var summary by BlogEntriesTable.summary
    var content by BlogEntriesTable.content
}
