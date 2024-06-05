package server.database

import ServerDatabase
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.entity.Area
import database.entity.Blocking
import database.entity.DatabaseHelper.createTestArea
import database.entity.DatabaseHelper.createTestPath
import database.entity.DatabaseHelper.createTestSector
import database.entity.DatabaseHelper.createTestZone
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import java.time.Instant
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase

class TestDatabase: ApplicationTestBase() {
    @Test
    @Suppress("LongMethod")
    fun `test creating data classes`() = test {
        val area = createTestArea()
        ServerDatabase.instance.query {
            Area.findById(area.id).let {
                assertNotNull(it)
                assertEquals(it.displayName, DataProvider.SampleArea.displayName)
                assertEquals(it.webUrl.toString(), DataProvider.SampleArea.webUrl)
            }
        }

        val zone = createTestZone(area)
        ServerDatabase.instance.query {
            Zone.findById(zone.id).let {
                assertNotNull(it)
                assertEquals(it.displayName, DataProvider.SampleZone.displayName)
                assertEquals(it.webUrl.toString(), DataProvider.SampleZone.webUrl)
                assertEquals(it.point, DataProvider.SampleZone.point)
                assertContentEquals(it.points, DataProvider.SampleZone.points.asIterable())
            }
        }

        val sector = createTestSector(zone)
        ServerDatabase.instance.query {
            Sector.findById(sector.id).let {
                assertNotNull(it)
                assertEquals(it.displayName, DataProvider.SampleSector.displayName)
                assertEquals(it.point, DataProvider.SampleSector.point)
                assertEquals(it.kidsApt, DataProvider.SampleSector.kidsApt)
                assertEquals(it.sunTime, DataProvider.SampleSector.sunTime)
                assertEquals(it.walkingTime, DataProvider.SampleSector.walkingTime)
            }
        }

        val path = createTestPath(sector)
        ServerDatabase.instance.query {
            Path.findById(path.id).let {
                assertNotNull(it)

                assertEquals(it.displayName, DataProvider.SamplePath.displayName)
                assertEquals(it.sketchId, DataProvider.SamplePath.sketchId)

                assertEquals(it.height, DataProvider.SamplePath.height)
                assertEquals(it.grade, DataProvider.SamplePath.grade)
                assertEquals(it.ending, DataProvider.SamplePath.ending)

                assertEquals(it.pitches, DataProvider.SamplePath.pitches)

                assertEquals(it.stringCount, DataProvider.SamplePath.stringCount)

                assertEquals(it.paraboltCount, DataProvider.SamplePath.paraboltCount)
                assertEquals(it.burilCount, DataProvider.SamplePath.burilCount)
                assertEquals(it.pitonCount, DataProvider.SamplePath.pitonCount)
                assertEquals(it.spitCount, DataProvider.SamplePath.spitCount)
                assertEquals(it.tensorCount, DataProvider.SamplePath.tensorCount)

                assertEquals(it.crackerRequired, DataProvider.SamplePath.crackerRequired)
                assertEquals(it.friendRequired, DataProvider.SamplePath.friendRequired)
                assertEquals(it.lanyardRequired, DataProvider.SamplePath.lanyardRequired)
                assertEquals(it.nailRequired, DataProvider.SamplePath.nailRequired)
                assertEquals(it.pitonRequired, DataProvider.SamplePath.pitonRequired)
                assertEquals(it.stapesRequired, DataProvider.SamplePath.stapesRequired)

                assertEquals(it.builder, DataProvider.SamplePath.builder)
                assertEquals(it.reBuilder, DataProvider.SamplePath.reBuilder)
            }
        }
    }

    @Test
    fun `test Blocking with recurrence`() = test {
        val area = createTestArea()
        val zone = createTestZone(area)
        val sector = createTestSector(zone)
        val path = createTestPath(sector)

        val blocking = ServerDatabase.instance.query {
            Blocking.new {
                this.timestamp = Instant.ofEpochMilli(1710086772)
                this.type = BlockingTypes.BIRD
                this.recurrence = BlockingRecurrenceYearly(1U, Month.JANUARY, 2U, Month.FEBRUARY)
                this.path = path
            }
        }
        ServerDatabase.instance.query {
            Blocking.findById(blocking.id).let {
                assertNotNull(it)

                assertEquals(it.timestamp, Instant.ofEpochMilli(1710086772))
                assertEquals(it.type, BlockingTypes.BIRD)
                assertEquals(it.recurrence, BlockingRecurrenceYearly(1U, Month.JANUARY, 2U, Month.FEBRUARY))
                assertNull(it.endDate)
                assertEquals(it.path.id.value, path.id.value)
            }
        }
    }
}
