package server.endpoints.blog

import ServerDatabase
import assertions.assertSuccess
import database.entity.BlogEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
            assertSuccess<BlogEntriesData> { data ->
                assertNotNull(data)

                val entries = data.entries
                assertEquals(2, entries.size)
                assertEquals("Test Summary 1", entries[0].summary)
                assertEquals("Test Content 1", entries[0].content)
                assertEquals("Test Summary 2", entries[1].summary)
                assertEquals("Test Content 2", entries[1].content)
            }
        }
    }
}
