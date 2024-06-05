package database.entity

import ServerDatabase
import java.io.File
import java.net.URL
import server.DataProvider
import storage.Storage

object DatabaseHelper {
    suspend fun createTestArea(): Area = ServerDatabase.instance.query {
        Area.new {
            displayName = DataProvider.SampleArea.displayName
            webUrl = URL(DataProvider.SampleArea.webUrl)

            // Required, but not used
            image = File(Storage.ImagesDir, "abc")
        }
    }

    suspend fun createTestZone(area: Area): Zone = ServerDatabase.instance.query {
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

    suspend fun createTestSector(zone: Zone): Sector = ServerDatabase.instance.query {
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

    suspend fun createTestPath(sector: Sector): Path = ServerDatabase.instance.query {
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
}
