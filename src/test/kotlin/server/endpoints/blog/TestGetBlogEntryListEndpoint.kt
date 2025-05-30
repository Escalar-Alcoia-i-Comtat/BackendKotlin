package server.endpoints.blog

import ServerDatabase
import database.entity.BlogEntry
import io.ktor.client.call.body
import kotlin.test.Test
import kotlin.test.assertEquals
import server.base.ApplicationTestBase
import server.response.query.BlogEntriesData

class TestGetBlogEntryListEndpoint : ApplicationTestBase() {
    @Test
    fun `test creating blog entries`() = test {
        ServerDatabase {
            BlogEntry.new {
                summary = "Test Summary 1"
                content = "Test Content 1"
            }
            BlogEntry.new {
                summary = "Test Summary 2"
                content = "Test Content 2"
            }
        }

        get("/blog").apply {
            body<BlogEntriesData>().entries.let { entries ->
                assertEquals(2, entries.size)
                assertEquals("Test Summary 1", entries[0].summary)
                assertEquals("Test Content 1", entries[0].content)
                assertEquals("Test Summary 2", entries[1].summary)
                assertEquals("Test Content 2", entries[1].content)
            }
        }
    }
}
