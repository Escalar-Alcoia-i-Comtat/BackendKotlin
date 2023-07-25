package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.patch

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertSuccess
import com.arnyminerz.escalaralcoiaicomtat.backend.data.DataPoint
import com.arnyminerz.escalaralcoiaicomtat.backend.data.LatLng
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Zone
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.HashUtils
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.MessageDigestAlgorithm
import com.arnyminerz.escalaralcoiaicomtat.backend.storage.Storage
import com.arnyminerz.escalaralcoiaicomtat.backend.utils.toJson
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPatchZoneEndpoint: ApplicationTestBase() {
    @Test
    fun `test patching Zone - update display name`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        client.submitFormWithBinaryData(
            url = "/zone/$zoneId",
            formData = formData {
                append("displayName", "New Display Name")
            }
        ).apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertEquals("New Display Name", zone.displayName)
        }
    }

    @Test
    fun `test patching Zone - update web url`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        client.submitFormWithBinaryData(
            url = "/zone/$zoneId",
            formData = formData {
                append("webUrl", "https://example.com/new")
            }
        ).apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertEquals("https://example.com/new", zone.webUrl.toString())
        }
    }

    @Test
    fun `test patching Zone - update point`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        client.submitFormWithBinaryData(
            url = "/zone/$zoneId",
            formData = formData {
                append(
                    "point",
                    DataProvider.SampleZone
                        .point
                        .copy(latitude = 0.98765, longitude = 0.43210)
                        .toJson()
                        .toString()
                )
            }
        ).apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertEquals(LatLng(latitude = 0.98765, longitude = 0.43210), zone.point)
        }
    }

    @Test
    fun `test patching Zone - update points`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        client.submitFormWithBinaryData(
            url = "/zone/$zoneId",
            formData = formData {
                append(
                    "points",
                    DataProvider.SampleZone
                        .points
                        .toMutableSet()
                        .apply { remove(first()) }
                        .toJson()
                        .also { println("New points: $it") }
                        .toString()
                )
            }
        ).apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)
            assertEquals(1, zone.points.size)
            assertEquals(
                DataPoint(LatLng(0.12345, 0.67890), "Label 2", "testing-icon"),
                zone.points.first()
            )
        }
    }

    @Test
    fun `test patching Zone - update image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val image = this::class.java.getResourceAsStream("/images/cocentaina.jpg")!!.use {
            it.readBytes()
        }

        client.submitFormWithBinaryData(
            url = "/zone/$zoneId",
            formData = formData {
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=zone.jpg")
                })
            }
        ).apply {
            assertSuccess()
        }

        var zoneImage: String? = null

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)

            val imageFile = zone.image
            zoneImage = imageFile.toRelativeString(Storage.ImagesDir)
            assertTrue(imageFile.exists())
        }

        client.get("/file/$zoneImage").apply {
            assertSuccess { data ->
                assertNotNull(data)
                val serverHash = data.getString("hash")
                val localHash = HashUtils.getCheckSumFromStream(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    this::class.java.getResourceAsStream("/images/cocentaina.jpg")!!
                )
                assertEquals(localHash, serverHash)
            }
        }
    }

    @Test
    fun `test patching Zone - update track`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val track = this::class.java.getResourceAsStream("/tracks/balconet.kmz")!!.use {
            it.readBytes()
        }

        client.submitFormWithBinaryData(
            url = "/zone/$zoneId",
            formData = formData {
                append("kmz", track, Headers.build {
                    append(HttpHeaders.ContentType, "application/vnd")
                    append(HttpHeaders.ContentDisposition, "filename=track.kmz")
                })
            }
        ).apply {
            assertSuccess()
        }

        var zoneTrack: String? = null

        ServerDatabase.instance.query {
            val zone = Zone[zoneId]
            assertNotNull(zone)

            val trackFile = zone.kmz
            zoneTrack = trackFile.toRelativeString(Storage.TracksDir)
            assertTrue(trackFile.exists())
        }

        client.get("/file/$zoneTrack").apply {
            assertSuccess { data ->
                assertNotNull(data)
                val serverHash = data.getString("hash")
                val localHash = HashUtils.getCheckSumFromStream(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    this::class.java.getResourceAsStream("/tracks/balconet.kmz")!!
                )
                assertEquals(localHash, serverHash)
            }
        }
    }
}