package server.database

import ServerDatabase
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import java.io.File
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import server.DataProvider
import server.base.ApplicationTestBase
import storage.Storage

class TestDatabase: ApplicationTestBase() {
    @Test
    @Suppress("LongMethod")
    fun `test creating data classes`() = test {
        val area = ServerDatabase.instance.query {
            Area.new {
                displayName = DataProvider.SampleArea.displayName
                webUrl = URL(DataProvider.SampleArea.webUrl)

                // Required, but not used
                image = File(Storage.ImagesDir, "abc")
            }
        }
        ServerDatabase.instance.query {
            Area.findById(area.id).let {
                assertNotNull(it)
                assertEquals(it.displayName, DataProvider.SampleArea.displayName)
                assertEquals(it.webUrl.toString(), DataProvider.SampleArea.webUrl)
            }
        }

        val zone = ServerDatabase.instance.query {
            Zone.new {
                displayName = DataProvider.SampleZone.displayName
                webUrl = URL(DataProvider.SampleZone.webUrl)
                point = DataProvider.SampleZone.point
                pointsSet = DataProvider.SampleZone.points.map { it.toJson().toString() }

                // Required, but not used
                image = File(Storage.ImagesDir, "abc")
                kmz = File(Storage.TracksDir, "abc")

                // Must specify a parent
                this.area = area
            }
        }
        ServerDatabase.instance.query {
            Zone.findById(zone.id).let {
                assertNotNull(it)
                assertEquals(it.displayName, DataProvider.SampleZone.displayName)
                assertEquals(it.webUrl.toString(), DataProvider.SampleZone.webUrl)
                assertEquals(it.point, DataProvider.SampleZone.point)
                assertContentEquals(it.points, DataProvider.SampleZone.points.asIterable())
            }
        }

        val sector = ServerDatabase.instance.query {
            Sector.new {
                displayName = DataProvider.SampleSector.displayName
                point = DataProvider.SampleSector.point
                kidsApt = DataProvider.SampleSector.kidsApt
                sunTime = DataProvider.SampleSector.sunTime
                walkingTime = DataProvider.SampleSector.walkingTime

                // Required, but not used
                image = File(Storage.ImagesDir, "abc")

                // Must specify a parent
                this.zone = zone
            }
        }
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

        val path = ServerDatabase.instance.query {
            Path.new {
                displayName = DataProvider.SamplePath.displayName
                sketchId = DataProvider.SamplePath.sketchId

                height = DataProvider.SamplePath.height
                grade = DataProvider.SamplePath.grade
                ending = DataProvider.SamplePath.ending

                pitches = DataProvider.SamplePath.pitches

                stringCount = DataProvider.SamplePath.stringCount

                paraboltCount = DataProvider.SamplePath.paraboltCount
                burilCount = DataProvider.SamplePath.burilCount
                pitonCount = DataProvider.SamplePath.pitonCount
                spitCount = DataProvider.SamplePath.spitCount
                tensorCount = DataProvider.SamplePath.tensorCount

                crackerRequired = DataProvider.SamplePath.crackerRequired
                friendRequired = DataProvider.SamplePath.friendRequired
                lanyardRequired = DataProvider.SamplePath.lanyardRequired
                nailRequired = DataProvider.SamplePath.nailRequired
                pitonRequired = DataProvider.SamplePath.pitonRequired
                stapesRequired = DataProvider.SamplePath.stapesRequired

                builder = DataProvider.SamplePath.builder
                reBuilder = DataProvider.SamplePath.reBuilder

                // Must specify a parent
                this.sector = sector
            }
        }
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
}
