package server.endpoints.create

import ServerDatabase
import assertions.assertFailure
import database.EntityTypes
import database.entity.Area
import database.entity.info.LastUpdate
import distribution.Notifier
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors

class TestAreaCreationEndpoint: ApplicationTestBase() {
    @Test
    fun `test area creation`() = test {
        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        ServerDatabase.instance.query {
            val area = Area[areaId]
            assertNotNull(area)
            assertEquals(DataProvider.SampleArea.displayName, area.displayName)
            assertEquals(URL(DataProvider.SampleArea.webUrl), area.webUrl)

            val imageFile = area.image
            assertTrue(imageFile.exists())

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }

        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.AREA, areaId)
    }

    @Test
    fun `test area creation - missing arguments`() = test {
        DataProvider.provideSampleArea(skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.AREA)
            null
        }
        DataProvider.provideSampleArea(skipWebUrl = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.AREA)
            null
        }
        DataProvider.provideSampleArea(skipImage = true) {
            assertFailure(Errors.MissingData)
            assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.AREA)
            null
        }
    }
}
