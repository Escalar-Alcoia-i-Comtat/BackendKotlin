package server.endpoints.blog

import ServerDatabase
import database.entity.BlogEntry
import io.ktor.server.routing.RoutingContext
import server.endpoints.EndpointBase
import server.response.query.BlogEntriesData
import server.response.respondSuccess

object GetBlogEntryListEndpoint : EndpointBase("/blog") {
    override suspend fun RoutingContext.endpoint() {
        // Get all the blog entries
        val blogEntries = ServerDatabase { BlogEntry.all().toList() }

        respondSuccess(
            data = BlogEntriesData(blogEntries)
        )
    }
}
