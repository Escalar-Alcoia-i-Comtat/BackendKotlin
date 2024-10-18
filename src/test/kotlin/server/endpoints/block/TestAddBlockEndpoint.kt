package server.endpoints.block

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.EntityTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import database.table.BlockingTable
import distribution.Notifier
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import java.time.LocalDateTime
import java.time.Month
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.error.Errors
import server.request.AddBlockRequest
import server.response.update.UpdateResponseData

class TestAddBlockEndpoint : ApplicationTestBase() {
    @Test
    fun `test adding block to path - no ending, no recurrence`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(BlockingTypes.BUILD)
            )
        }.apply {
            assertSuccess<UpdateResponseData<Blocking>>(HttpStatusCode.Created) {
                assertNotNull(it)
                assertEquals(BlockingTypes.BUILD, it.element.type)
                assertNull(it.element.recurrence)
                assertNull(it.element.endDate)
            }
        }

        var blockId: Int? = null
        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull().also { blockId = it?.id?.value }
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertNull(block.recurrence)
            assertNull(block.endDate)

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }

        assertNotNull(blockId)
        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.BLOCKING, blockId!!)
    }

    @Test
    fun `test adding block to path - missing type`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        post("/block/$pathId") {
            /*setBody(
                JsonObject(emptyMap())
            )*/
        }.apply {
            assertFailure(Errors.MissingData)
        }
        assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.BLOCKING)
    }

    @Test
    fun `test adding block to path - invalid path`() = test {
        post("/block/123") {
            setBody(
                AddBlockRequest(BlockingTypes.BUILD)
            )
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
        assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.BLOCKING)
    }

    @Test
    fun `test adding block to path - both ending and recurrence`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(
                    type = BlockingTypes.BUILD,
                    endDate = LocalDateTime.now(),
                    recurrence = BlockingRecurrenceYearly(1U, Month.JANUARY, 3U, Month.FEBRUARY)
                )
            )
        }.apply {
            assertFailure(Errors.Conflict)
        }
        assertNotificationNotSent(Notifier.TOPIC_CREATED, EntityTypes.BLOCKING)
    }

    @Test
    fun `test adding block to path - with ending, no recurrence`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        val endDate = LocalDateTime.now()

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(
                    type = BlockingTypes.BUILD,
                    endDate = endDate
                )
            )
        }.apply {
            assertSuccess<UpdateResponseData<Blocking>>(HttpStatusCode.Created) {
                assertNotNull(it)
                assertEquals(BlockingTypes.BUILD, it.element.type)
                assertNull(it.element.recurrence)
                assertEquals(endDate.truncatedTo(ChronoUnit.MILLIS), it.element.endDate)
            }
        }

        var blockId: Int? = null
        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull().also { blockId = it?.id?.value }
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertNull(block.recurrence)
            assertEquals(endDate?.truncatedTo(ChronoUnit.MILLIS), block.endDate)
        }

        assertNotNull(blockId)
        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.BLOCKING, blockId!!)
    }

    @Test
    fun `test adding block to path - no ending, with recurrence`() = test {
        val areaId = DataProvider.provideSampleArea(this)
        val zoneId = DataProvider.provideSampleZone(this, areaId)
        val sectorId = DataProvider.provideSampleSector(this, zoneId)
        val pathId = DataProvider.provideSamplePath(this, sectorId)

        val recurrence = BlockingRecurrenceYearly(1U, Month.JANUARY, 3U, Month.FEBRUARY)

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(
                    type = BlockingTypes.BUILD,
                    recurrence = recurrence
                )
            )
        }.apply {
            assertSuccess<UpdateResponseData<Blocking>>(HttpStatusCode.Created) {
                assertNotNull(it)
                assertEquals(BlockingTypes.BUILD, it.element.type)
                assertEquals(recurrence, it.element.recurrence)
                assertNull(it.element.endDate)
            }
        }

        var blockId: Int? = null
        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull().also { blockId = it?.id?.value }
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertEquals(recurrence, block.recurrence)
            assertNull(block.endDate)
        }

        assertNotNull(blockId)
        assertNotificationSent(Notifier.TOPIC_CREATED, EntityTypes.BLOCKING, blockId!!)
    }
}
