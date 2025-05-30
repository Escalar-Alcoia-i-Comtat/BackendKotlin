package server.endpoints.blog

import ServerDatabase
import assertions.assertSuccess
import database.entity.BlogEntry
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import server.base.ApplicationTestBase
import server.request.AddBlogPostRequest
import server.response.update.UpdateResponseData

class TestAddBlogEntryEndpoint : ApplicationTestBase() {
    @Test
    fun `test creating blog entry`() = test {
        var id: Int? = null
        post("/blog") {
            setBody(
                AddBlogPostRequest(
                    summary = "Test Summary",
                    content = "Test Content"
                )
            )
        }.apply {
            assertSuccess<UpdateResponseData<BlogEntry>>(HttpStatusCode.Created) {
                assertNotNull(it)
                assertEquals("Test Summary", it.element.summary)
                assertEquals("Test Content", it.element.content)

                id = it.element.id.value
            }
        }

        id ?: error("Blog entry ID should not be null")

        val blogEntry = ServerDatabase { BlogEntry.findById(id) }
        assertNotNull(blogEntry)
        assertEquals(id, blogEntry.id.value)
        assertEquals("Test Summary", blogEntry.summary)
        assertEquals("Test Content", blogEntry.content)
    }
}
