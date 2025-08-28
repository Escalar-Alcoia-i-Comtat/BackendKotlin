package server

import assertions.assertSuccess
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import data.Builder
import data.DataPoint
import data.Ending
import data.EndingInclination
import data.EndingInfo
import data.ExternalTrack
import data.Grade
import data.LatLng
import data.PhoneCarrier
import data.PhoneSignalAvailability
import data.PhoneSignalStrength
import data.PitchInfo
import database.entity.Area
import database.entity.Path
import database.entity.Sector
import database.entity.Zone
import database.serialization.Json
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.assertNotNull
import server.base.ApplicationTestBase.Companion.AUTH_TOKEN
import server.base.StubApplicationTestBuilder
import server.response.update.UpdateResponseData

@Suppress("LongParameterList", "MayBeConst", "MayBeConstant")
object DataProvider {
    object SampleArea {
        val displayName = "Testing Area"
        val webUrl = "https://example.com"
    }

    suspend fun provideSampleArea(
        builder: StubApplicationTestBuilder,
        skipDisplayName: Boolean = false,
        skipWebUrl: Boolean = false,
        skipImage: Boolean = false,
        imageFile: String = "/images/alcoi.jpg",
        assertion: suspend HttpResponse.() -> Int? = {
            var areaId: Int? = null
            assertSuccess<UpdateResponseData<Area>>(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                areaId = data.element.id.value
            }
            assertNotNull(areaId)
            areaId
        }
    ): Int? {
        val image = this::class.java.getResourceAsStream(imageFile)!!.use {
            it.readBytes()
        }

        var areaId: Int?

        builder.client.submitFormWithBinaryData(
            url = "/area",
            formData = formData {
                if (!skipDisplayName)
                    append("displayName", SampleArea.displayName)
                if (!skipWebUrl)
                    append("webUrl", SampleArea.webUrl)
                if (!skipImage)
                    append("image", image, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=area.jpg")
                    })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            areaId = assertion()
        }

        return areaId
    }

    object SampleZone {
        val displayName = "Testing Zone"
        val webUrl = "https://example.com"
        val point = LatLng(0.12345, 0.67890)
        val points = listOf(
            DataPoint(LatLng(0.12345, 0.67890), "Label 1", "testing-icon"),
            DataPoint(LatLng(0.12345, 0.67890), "Label 2", "testing-icon")
        )
    }

    suspend fun provideSampleZone(
        builder: StubApplicationTestBuilder,
        areaId: Int?,
        skipDisplayName: Boolean = false,
        skipWebUrl: Boolean = false,
        skipImage: Boolean = false,
        skipKmz: Boolean = false,
        emptyPoints: Boolean = false,
        assertion: suspend HttpResponse.() -> Int? = {
            var zoneId: Int? = null
            assertSuccess<UpdateResponseData<Zone>>(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                zoneId = data.element.id.value
            }
            assertNotNull(zoneId)
            zoneId
        }
    ): Int? {
        val image = this::class.java.getResourceAsStream("/images/uixola.jpg")!!.use {
            it.readBytes()
        }
        val kmz = this::class.java.getResourceAsStream("/tracks/testing.kmz")!!.use {
            it.readBytes()
        }

        var zoneId: Int?

        builder.client.submitFormWithBinaryData(
            url = "/zone",
            formData = formData {
                if (areaId != null)
                    append("area", areaId)
                if (!skipDisplayName)
                    append("displayName", SampleZone.displayName)
                if (!skipWebUrl)
                    append("webUrl", SampleZone.webUrl)
                append("point", Json.encodeToString(SampleZone.point))
                append(
                    "points",
                    Json.encodeToString(if (emptyPoints) emptyList() else SampleZone.points)
                )
                if (!skipImage)
                    append("image", image, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=zone.jpg")
                    })
                if (!skipKmz)
                    append("kmz", kmz, Headers.build {
                        append(HttpHeaders.ContentType, "application/vnd")
                        append(HttpHeaders.ContentDisposition, "filename=zone.kmz")
                    })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            zoneId = assertion()
        }

        return zoneId
    }

    object SampleSector {
        val displayName = "Testing Sector"
        val point = LatLng(0.12345, 0.67890)
        val kidsApt = true
        val sunTime = Sector.SunTime.Afternoon
        val walkingTime = 12U

        val phoneSignalAvailability = listOf(
            PhoneSignalAvailability(PhoneSignalStrength.SIGNAL_4G, PhoneCarrier.MOVISTAR),
            PhoneSignalAvailability(PhoneSignalStrength.NOT_AVAILABLE, PhoneCarrier.ORANGE),
            PhoneSignalAvailability(PhoneSignalStrength.NOT_AVAILABLE, PhoneCarrier.VODAFONE),
        )

        val tracks = listOf(
            ExternalTrack(ExternalTrack.Type.Wikiloc, "https://example.com")
        )
    }

    suspend fun provideSampleSector(
        builder: StubApplicationTestBuilder,
        zoneId: Int?,
        skipDisplayName: Boolean = false,
        skipKidsApt: Boolean = false,
        skipSunTime: Boolean = false,
        skipImage: Boolean = false,
        skipTracks: Boolean = false,
        skipGpx: Boolean = false,
        assertion: suspend HttpResponse.() -> Int? = {
            var sectorId: Int? = null
            assertSuccess<UpdateResponseData<Sector>>(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                sectorId = data.element.id.value
            }
            assertNotNull(sectorId)
            sectorId
        }
    ): Int? {
        val image = this::class.java.getResourceAsStream("/images/desploms1.jpg")!!.use {
            it.readBytes()
        }
        val gpx = this::class.java.getResourceAsStream("/tracks/ulldelmoro.gpx")!!.use {
            it.readBytes()
        }

        var sectorId: Int?

        builder.client.submitFormWithBinaryData(
            url = "/sector",
            formData = formData {
                if (zoneId != null)
                    append("zone", zoneId)
                if (!skipDisplayName)
                    append("displayName", SampleSector.displayName)
                if (!skipKidsApt)
                    append("kidsApt", SampleSector.kidsApt)
                if (!skipSunTime)
                    append("sunTime", SampleSector.sunTime.name)
                if (!skipTracks)
                    append("tracks", SampleSector.tracks.joinToString("\n") { "${it.type.name};${it.url}" })
                append("walkingTime", SampleSector.walkingTime.toInt())
                append("point", Json.encodeToString(SampleSector.point))
                if (!skipImage)
                    append("image", image, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=sector.jpg")
                    })
                if (!skipGpx)
                    append("gpx", gpx, Headers.build {
                        append(HttpHeaders.ContentType, "application/gpx+xml")
                        append(HttpHeaders.ContentDisposition, "filename=sector.gpx")
                    })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            sectorId = assertion()
        }

        return sectorId
    }

    object SamplePath {
        val displayName = "Sample Path"
        val sketchId = 3U

        val height = 123U
        val grade = Grade.G6A
        val aidGrade = Grade.A2
        val ending = Ending.CHAIN_CARABINER

        val pitches = listOf(
            PitchInfo(
                1U,
                Grade.G7A_PLUS,
                Grade.A2,
                12U,
                Ending.CHAIN_CARABINER,
                EndingInfo.EQUIPPED,
                EndingInclination.DIAGONAL
            )
        )

        val stringCount = 13U

        val paraboltCount = 3U
        val burilCount = 2U
        val pitonCount = 3U
        val spitCount = 2U
        val tensorCount = 3U

        val crackerRequired = true
        val friendRequired = false
        val lanyardRequired = true
        val nailRequired = true
        val pitonRequired = false
        val stapesRequired = true

        val showDescription = true
        val description = "this is a sample description"

        val builder: Builder = Builder("Name", "April 2023")
        val reBuilder: List<Builder> = listOf(
            Builder("Name 1 ", "April 2023"),
            Builder("Name 2", "June 2023")
        )
    }

    suspend fun provideSamplePath(
        builder: StubApplicationTestBuilder,
        sectorId: Int?,
        skipDisplayName: Boolean = false,
        skipSketchId: Boolean = false,
        /**
         * Should contain the path in resources of all the images to include.
         */
        images: List<String>? = null,
        assertion: suspend HttpResponse.() -> Int? = {
            var pathId: Int? = null
            assertSuccess<UpdateResponseData<Path>>(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                pathId = data.element.id.value
            }
            assertNotNull(pathId)
            pathId
        }
    ): Int? {
        var pathId: Int?

        val imagesData = images?.map { path ->
            this::class.java.getResourceAsStream(path)!!.use { it.readBytes() }
        }

        builder.client.submitFormWithBinaryData(
            url = "/path",
            formData = formData {
                if (sectorId != null)
                    append("sector", sectorId)
                if (!skipDisplayName)
                    append("displayName", SamplePath.displayName)
                if (!skipSketchId)
                    append("sketchId", SamplePath.sketchId.toInt())
                append("height", SamplePath.height.toInt())
                append("grade", SamplePath.grade.name)
                append("aidGrade", SamplePath.aidGrade.name)
                append("ending", SamplePath.ending.name)
                append("pitches", Json.encodeToString(SamplePath.pitches))
                append("stringCount", SamplePath.stringCount.toInt())
                append("paraboltCount", SamplePath.paraboltCount.toInt())
                append("burilCount", SamplePath.burilCount.toInt())
                append("pitonCount", SamplePath.pitonCount.toInt())
                append("spitCount", SamplePath.spitCount.toInt())
                append("tensorCount", SamplePath.tensorCount.toInt())
                append("crackerRequired", SamplePath.crackerRequired)
                append("friendRequired", SamplePath.friendRequired)
                append("lanyardRequired", SamplePath.lanyardRequired)
                append("nailRequired", SamplePath.nailRequired)
                append("pitonRequired", SamplePath.pitonRequired)
                append("stapesRequired", SamplePath.stapesRequired)
                append("showDescription", SamplePath.showDescription)
                append("description", SamplePath.description)
                append("builder", Json.encodeToString(SamplePath.builder))
                append("reBuilder", Json.encodeToString(SamplePath.reBuilder))

                imagesData?.forEachIndexed { index, image ->
                    append("image", image, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=path-$index.jpg")
                    })
                }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            pathId = assertion()
        }

        return pathId
    }

    object SampleBlocking {
        val timestamp: Instant = Instant.ofEpochSecond(1710086772)
        val type: BlockingTypes = BlockingTypes.BIRD
        val recurrence: BlockingRecurrenceYearly = BlockingRecurrenceYearly(1U, Month.JANUARY, 2U, Month.FEBRUARY)
        val endDate: LocalDateTime = LocalDateTime.of(2024, 6, 5, 23, 59)
    }
}
