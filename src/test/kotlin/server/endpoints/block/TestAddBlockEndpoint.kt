package server.endpoints.block

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import database.table.BlockingTable
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
import utils.getJSONObjectOrNull
import utils.jsonOf

class TestAddBlockEndpoint: ApplicationTestBase() {
    @Test
    fun `test adding block to path - no ending, no recurrence`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD
                ).toString()
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created) {
                assertNotNull(it?.getJSONObjectOrNull("element"))
            }
        }

        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull()
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertNull(block.recurrence)
            assertNull(block.endDate)

            assertNotEquals(LastUpdate.get(), lastUpdate)
        }
    }

    @Test
    fun `test adding block to path - missing type`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        post("/block/$pathId") {
            setBody(
                jsonOf().toString()
            )
        }.apply {
            assertFailure(Errors.MissingData)
        }
    }

    @Test
    fun `test adding block to path - invalid path`() = test {
        post("/block/123") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD
                ).toString()
            )
        }.apply {
            assertFailure(Errors.ObjectNotFound)
        }
    }

    @Test
    fun `test adding block to path - both ending and recurrence`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD,
                    "end_date" to LocalDateTime.now(),
                    "recurrence" to BlockingRecurrenceYearly(1U, Month.JANUARY, 3U, Month.FEBRUARY)
                ).toString()
            )
        }.apply {
            assertFailure(Errors.Conflict)
        }
    }

    @Test
    fun `test adding block to path - with ending, no recurrence`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        val endDate = LocalDateTime.now()

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD,
                    "end_date" to endDate
                ).toString()
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created) {
                assertNotNull(it?.getJSONObjectOrNull("element"))
            }
        }

        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull()
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertNull(block.recurrence)
            assertEquals(endDate?.truncatedTo(ChronoUnit.MILLIS), block.endDate)
        }
    }

    @Test
    fun `test adding block to path - no ending, with recurrence`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        val recurrence = BlockingRecurrenceYearly(1U, Month.JANUARY, 3U, Month.FEBRUARY)

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD,
                    "recurrence" to recurrence
                ).toString()
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created) {
                assertNotNull(it?.getJSONObjectOrNull("element"))
            }
        }

        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull()
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertEquals(recurrence, block.recurrence)
            assertNull(block.endDate)
        }
    }
}
