package database.entity

import ServerDatabase
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import data.Builder
import data.DataPoint
import data.Ending
import data.Grade
import data.LatLng
import data.PhoneSignalAvailability
import data.PitchInfo
import java.io.File
import java.net.URI
import java.net.URL
import java.time.Instant
import java.time.LocalDateTime
import server.DataProvider
import storage.Storage

object DatabaseHelper {
    fun createTestFile(dir: File, name: String): File {
        return File(dir, name)
            .also { it.parentFile.mkdirs() }
            .also(File::createNewFile)
            .also(File::deleteOnExit)
    }

    suspend fun createTestArea(
        displayName: String = DataProvider.SampleArea.displayName,
        webUrl: String = DataProvider.SampleArea.webUrl
    ): Area = ServerDatabase.instance.query {
        Area.new {
            this.timestamp = Instant.ofEpochSecond(1738829281)

            this.displayName = displayName
            this.webUrl = URI.create(webUrl).toURL()

            // Required, but not used
            image = createTestFile(Storage.ImagesDir, "8eb8e766-373a-43cd-9378-097b850a7b60")
        }
    }

    suspend fun createTestZone(
        area: Area,
        displayName: String = DataProvider.SampleZone.displayName,
        webUrl: String = DataProvider.SampleZone.webUrl,
        point: LatLng = DataProvider.SampleZone.point,
        points: List<DataPoint> = DataProvider.SampleZone.points
    ): Zone = ServerDatabase.instance.query {
        Zone.new {
            this.displayName = displayName
            this.webUrl = URL(webUrl)
            this.point = point
            this.points = points

            // Required, but not used
            image = createTestFile(Storage.ImagesDir, "abc")
            kmz = createTestFile(Storage.TracksDir, "abc")

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
        walkingTime: UInt = DataProvider.SampleSector.walkingTime,
        phoneSignalAvailability: List<PhoneSignalAvailability>? = DataProvider.SampleSector.phoneSignalAvailability
    ): Sector = ServerDatabase.instance.query {
        Sector.new {
            this.displayName = displayName
            this.point = point
            this.kidsApt = kidsApt
            this.sunTime = sunTime
            this.walkingTime = walkingTime
            this.phoneSignalAvailability = phoneSignalAvailability

            // Required, but not used
            image = createTestFile(Storage.ImagesDir, "abc")
            gpx = createTestFile(Storage.TracksDir, "abc")

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
        aidGrade: Grade = DataProvider.SamplePath.aidGrade,
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
            this.aidGrade = aidGrade
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
                createTestFile(Storage.ImagesDir, "abc"),
                createTestFile(Storage.ImagesDir, "def")
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
