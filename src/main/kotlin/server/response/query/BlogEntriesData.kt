package server.response.query

import KoverIgnore
import database.entity.BlogEntry
import kotlinx.serialization.Serializable
import server.response.ResponseData

@KoverIgnore
@Serializable
data class BlogEntriesData(
    val entries: List<BlogEntry>
): ResponseData
