package database.entity

import ServerDatabase
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import data.Builder
import data.DataPoint
import data.Ending
import data.Grade
import data.LatLng
import data.PitchInfo
import java.io.File
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import server.DataProvider
import storage.Storage

object DatabaseHelper {
    suspend fun createTestArea(
        displayName: String = DataProvider.SampleArea.displayName,
        webUrl: String = DataProvider.SampleArea.webUrl
    ): Area = ServerDatabase.instance.query {
        Area.new {
            this.displayName = displayName
            this.webUrl = URL(webUrl)

            // Required, but not used
            image = File(Storage.ImagesDir, "abc")
        }
    }

    suspend fun createTestZone(
        area: Area,
        displayName: String = DataProvider.SampleZone.displayName,
        webUrl: String = DataProvider.SampleZone.webUrl,
        point: LatLng = DataProvider.SampleZone.point,
        pointsSet: Set<DataPoint> = DataProvider.SampleZone.points
    ): Zone = ServerDatabase.instance.query {
        Zone.new {
            this.displayName = displayName
            this.webUrl = URL(webUrl)
            this.point = point
            this.pointsSet = pointsSet.map { it.toJson().toString() }

            // Required, but not used
            image = File(Storage.ImagesDir, "abc")
            kmz = File(Storage.TracksDir, "abc")

            // Must specify a parent
            this.area = area
        }
    }

    suspend fun createTestSector(
        zone: Zone,
        displayName: String = DataProvider.SampleSector.displayName,
        point: LatLng = DataProvider.SampleSector.point,
        kidsApt: Boolean = DataProvider.SampleSector.kidsApt,
        sunTime: Sector.SunTime = DataProvider.SampleSector.sunTime,
        walkingTime: UInt = DataProvider.SampleSector.walkingTime
    ): Sector = ServerDatabase.instance.query {
        Sector.new {
            this.displayName = displayName
            this.point = point
            this.kidsApt = kidsApt
            this.sunTime = sunTime
            this.walkingTime = walkingTime

            // Required, but not used
            image = File(Storage.ImagesDir, "abc")
            gpx = File(Storage.TracksDir, "abc")

            // Must specify a parent
            this.zone = zone
        }
    }

    suspend fun createTestPath(
        sector: Sector,
        displayName: String = DataProvider.SamplePath.displayName,
        sketchId: UInt = DataProvider.SamplePath.sketchId,
        height: UInt = DataProvider.SamplePath.height,
        grade: Grade = DataProvider.SamplePath.grade,
        ending: Ending = DataProvider.SamplePath.ending,
        pitches: List<PitchInfo> = DataProvider.SamplePath.pitches,
        stringCount: UInt = DataProvider.SamplePath.stringCount,
        paraboltCount: UInt = DataProvider.SamplePath.paraboltCount,
        burilCount: UInt = DataProvider.SamplePath.burilCount,
        pitonCount: UInt = DataProvider.SamplePath.pitonCount,
        spitCount: UInt = DataProvider.SamplePath.spitCount,
        tensorCount: UInt = DataProvider.SamplePath.tensorCount,
        crackerRequired: Boolean = DataProvider.SamplePath.crackerRequired,
        friendRequired: Boolean = DataProvider.SamplePath.friendRequired,
        lanyardRequired: Boolean = DataProvider.SamplePath.lanyardRequired,
        nailRequired: Boolean = DataProvider.SamplePath.nailRequired,
        pitonRequired: Boolean = DataProvider.SamplePath.pitonRequired,
        stapesRequired: Boolean = DataProvider.SamplePath.stapesRequired,
        builder: Builder = DataProvider.SamplePath.builder,
        reBuilder: List<Builder> = DataProvider.SamplePath.reBuilder,
        showDescription: Boolean = DataProvider.SamplePath.showDescription,
        description: String = DataProvider.SamplePath.description,
    ): Path = ServerDatabase.instance.query {
        Path.new {
            this.displayName = displayName
            this.sketchId = sketchId

            this.height = height
            this.grade = grade
            this.ending = ending

            this.pitches = pitches

            this.stringCount = stringCount

            this.paraboltCount = paraboltCount
            this.burilCount = burilCount
            this.pitonCount = pitonCount
            this.spitCount = spitCount
            this.tensorCount = tensorCount

            this.crackerRequired = crackerRequired
            this.friendRequired = friendRequired
            this.lanyardRequired = lanyardRequired
            this.nailRequired = nailRequired
            this.pitonRequired = pitonRequired
            this.stapesRequired = stapesRequired

            this.builder = builder
            this.reBuilder = reBuilder

            this.showDescription = showDescription
            this.description = description

            this.images = listOf(
                File(Storage.ImagesDir, "abc"),
                File(Storage.ImagesDir, "def")
            )

            // Must specify a parent
            this.sector = sector
        }
    }

    suspend fun createTestBlocking(
        path: Path,
        timestamp: Instant = DataProvider.SampleBlocking.timestamp,
        type: BlockingTypes = DataProvider.SampleBlocking.type,
        recurrence: BlockingRecurrenceYearly? = DataProvider.SampleBlocking.recurrence,
        endDate: LocalDateTime? = null
    ): Blocking = ServerDatabase.instance.query {
        Blocking.new {
            this.timestamp = timestamp
            this.type = type
            if (recurrence != null)
                this.recurrence = recurrence
            else
                this.endDate = endDate
            this.path = path
        }
    }
}
