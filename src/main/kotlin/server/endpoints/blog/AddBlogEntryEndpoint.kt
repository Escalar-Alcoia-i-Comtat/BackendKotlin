package server.endpoints.blog

import ServerDatabase
import database.EntityTypes
import database.entity.BlogEntry
import distribution.Notifier
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.RoutingContext
import server.endpoints.SecureEndpointBase
import server.error.Errors
import server.request.AddBlogPostRequest
import server.response.respondFailure
import server.response.respondSuccess
import server.response.update.UpdateResponseData

object AddBlogEntryEndpoint : SecureEndpointBase("/blog") {
    override suspend fun RoutingContext.endpoint() {
        val body = call.receive<AddBlogPostRequest>()
        val (summary, content) = body

        if (summary == null || content == null) {
            return respondFailure(Errors.MissingData)
        }

        val blogEntry = ServerDatabase {
            BlogEntry.new {
                this.summary = summary
                this.content = content
            }
        }

        Notifier.getInstance().notifyCreated(EntityTypes.BLOG_ENTRY, blogEntry.id.value)

        respondSuccess(
            data = UpdateResponseData(blogEntry),
            httpStatusCode = HttpStatusCode.Created
        )
    }
}
