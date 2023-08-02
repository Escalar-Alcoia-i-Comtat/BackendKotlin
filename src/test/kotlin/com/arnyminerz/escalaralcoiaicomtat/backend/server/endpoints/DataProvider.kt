package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints

import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Builder
import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.Ending
import com.arnyminerz.escalaralcoiaicomtat.backend.data.EndingInclination
import com.arnyminerz.escalaralcoiaicomtat.backend.data.EndingInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.data.PitchInfo
import com.arnyminerz.escalaralcoiaicomtat.backend.data.SportsGrade
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Sector
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase.Companion.AUTH_TOKEN
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.getIntOrNull
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.toJson
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import kotlin.test.assertNotNull

@Suppress("LongParameterList", "MayBeConst", "MayBeConstant")
object DataProvider {
    object SampleArea {
        val displayName = "Testing Area"
        val webUrl = "https://example.com"
    }

    context(ApplicationTestBuilder)
    suspend fun provideSampleArea(
        skipDisplayName: Boolean = false,
        skipWebUrl: Boolean = false,
        skipImage: Boolean = false,
        assertion: suspend HttpResponse.() -> Int? = {
            var areaId: Int? = null
            assertSuccess(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                areaId = data.getIntOrNull("area_id")
            }
            areaId
        }
    ): Int? {
        val image = this::class.java.getResourceAsStream("/images/alcoi.jpg")!!.use {
            it.readBytes()
        }

        var areaId: Int?

        client.submitFormWithBinaryData(
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
        val points = setOf(
            DataPoint(LatLng(0.12345, 0.67890), "Label 1", "testing-icon"),
            DataPoint(LatLng(0.12345, 0.67890), "Label 2", "testing-icon")
        )
    }

    context(ApplicationTestBuilder)
    suspend fun provideSampleZone(
        areaId: Int?,
        skipDisplayName: Boolean = false,
        skipWebUrl: Boolean = false,
        skipImage: Boolean = false,
        skipKmz: Boolean = false,
        emptyPoints: Boolean = false,
        assertion: suspend HttpResponse.() -> Int? = {
            var zoneId: Int? = null
            assertSuccess(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                zoneId = data.getIntOrNull("zone_id")
            }
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

        client.submitFormWithBinaryData(
            url = "/zone",
            formData = formData {
                if (areaId != null)
                    append("area", areaId)
                if (!skipDisplayName)
                    append("displayName", SampleZone.displayName)
                if (!skipWebUrl)
                    append("webUrl", SampleZone.webUrl)
                append("point", SampleZone.point.toJson().toString())
                append(
                    "points",
                    (if (emptyPoints) emptyList() else SampleZone.points).toJson().toString()
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
    }

    context(ApplicationTestBuilder)
    suspend fun provideSampleSector(
        zoneId: Int?,
        skipDisplayName: Boolean = false,
        skipKidsApt: Boolean = false,
        skipSunTime: Boolean = false,
        skipImage: Boolean = false,
        assertion: suspend HttpResponse.() -> Int? = {
            var sectorId: Int? = null
            assertSuccess(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                sectorId = data.getIntOrNull("sector_id")
            }
            sectorId
        }
    ): Int? {
        val image = this::class.java.getResourceAsStream("/images/desploms1.jpg")!!.use {
            it.readBytes()
        }

        var sectorId: Int?

        client.submitFormWithBinaryData(
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
                append("walkingTime", SampleSector.walkingTime.toInt())
                append("point", SampleSector.point.toJson().toString())
                if (!skipImage)
                    append("image", image, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=sector.jpg")
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
        val grade = SportsGrade.G6A

        val pitches = listOf(
            PitchInfo(
                1U,
                SportsGrade.G7A_PLUS,
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

        val builder: Builder = Builder("Name", "April 2023")
        val reBuilder: List<Builder> = listOf(
            Builder("Name 1 ", "April 2023"),
            Builder("Name 2", "June 2023")
        )
    }

    context(ApplicationTestBuilder)
    suspend fun provideSamplePath(
        sectorId: Int?,
        skipDisplayName: Boolean = false,
        skipSketchId: Boolean = false,
        assertion: suspend HttpResponse.() -> Int? = {
            var pathId: Int? = null
            assertSuccess(HttpStatusCode.Created) { data ->
                assertNotNull(data)
                pathId = data.getIntOrNull("path_id")
            }
            pathId
        }
    ): Int? {
        var pathId: Int?

        client.submitFormWithBinaryData(
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
                append("pitches", SamplePath.pitches.toJson().toString())
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
                append("builder", SamplePath.builder.toJson().toString())
                append("reBuilder", SamplePath.reBuilder.toJson().toString())
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            pathId = assertion()
        }

        return pathId
    }
}
