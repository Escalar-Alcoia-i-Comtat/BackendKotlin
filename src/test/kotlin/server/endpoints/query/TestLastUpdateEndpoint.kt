package server.endpoints.query

import ServerDatabase
import assertions.assertSuccess
import database.entity.info.LastUpdate
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.base.ApplicationTestBase
import utils.getLongOrNull

class TestLastUpdateEndpoint: ApplicationTestBase() {
    @Test
    fun `test last update`() = test {
        // At first, it will be null
        get("/last_update").apply {
            assertSuccess { data ->
                assertNotNull(data)

                val lastUpdate = data.getLongOrNull("last_update")
                assertNull(lastUpdate)
            }
        }

        // Set some sample value for now
        val timestamp = Instant.ofEpochMilli(1695722391000)
        ServerDatabase.instance.query { LastUpdate.set(timestamp) }

        // Now check that it has been updated
        get("/last_update").apply {
            assertSuccess { data ->
                assertNotNull(data)

                val lastUpdate = data.getLongOrNull("last_update")?.let(Instant::ofEpochMilli)
                assertEquals(timestamp, lastUpdate)
            }
        }
    }
}
