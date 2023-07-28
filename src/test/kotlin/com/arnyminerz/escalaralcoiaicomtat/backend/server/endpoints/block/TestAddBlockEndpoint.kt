package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.block

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingRecurrenceYearly
import com.arnyminerz.escalaralcoiaicomtat.backend.data.BlockingTypes
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Blocking
import com.arnyminerz.escalaralcoiaicomtat.backend.database.table.BlockingTable
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.jsonOf
import io.ktor.client.request.setBody
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TestAddBlockEndpoint: ApplicationTestBase() {
    @Test
    fun `test adding block to path - no ending, no recurrence`() = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD
                ).toString()
            )
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull()
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertNull(block.recurrence)
            assertNull(block.endDate)
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
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val blocks = Blocking.find { BlockingTable.path eq pathId }
            assertEquals(1, blocks.count())
            val block = blocks.firstOrNull()
            assertNotNull(block)
            assertEquals(BlockingTypes.BUILD, block.type)
            assertNull(block.recurrence)
            assertEquals(endDate, block.endDate)
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
            assertSuccess()
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
