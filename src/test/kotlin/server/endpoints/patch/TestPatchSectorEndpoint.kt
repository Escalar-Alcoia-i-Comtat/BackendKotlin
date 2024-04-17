package server.endpoints.patch

import ServerDatabase
import assertions.assertSuccess
import data.LatLng
import database.entity.Sector
import database.entity.info.LastUpdate
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import server.DataProvider
import server.base.ApplicationTestBase
import storage.HashUtils
import storage.MessageDigestAlgorithm
import storage.Storage

class TestPatchSectorEndpoint : ApplicationTestBase() {
    @Test
    fun `test patching Sector - update display name`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }
        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("displayName", "New Display Name")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals("New Display Name", sector.displayName)
            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - update point`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("point", LatLng(0.456, 0.789).toJson().toString())
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals(LatLng(0.456, 0.789), sector.point)
            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - update kids apt`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("kidsApt", false)
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals(false, sector.kidsApt)
            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - update sun time`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("sunTime", Sector.SunTime.Morning.name)
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals(Sector.SunTime.Morning, sector.sunTime)
            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - update walking time`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("walkingTime", 9510)
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals(9510U, sector.walkingTime)
            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - update weight`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("weight", "0123")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertEquals("0123", sector.weight)
            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - update image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        val image = this::class.java.getResourceAsStream("/images/desploms2.jpg")!!.use {
            it.readBytes()
        }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=sector.jpg")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        var sectorImage: String? = null

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)

            val imageFile = sector.image
            sectorImage = imageFile.toRelativeString(Storage.ImagesDir)
            assertTrue(imageFile.exists())

            assertNotEquals(oldTimestamp, sector.timestamp)
        }

        get("/file/$sectorImage").apply {
            assertSuccess { data ->
                assertNotNull(data)
                val serverHash = data.getString("hash")
                val localHash = HashUtils.getCheckSumFromStream(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    this::class.java.getResourceAsStream("/images/desploms2.jpg")!!
                )
                assertEquals(localHash, serverHash)
            }
        }
    }

    @Test
    fun `test patching Sector - update gpx`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        val gpx = this::class.java.getResourceAsStream("/tracks/ulldelmoro.gpx")!!.use {
            it.readBytes()
        }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("gpx", gpx, Headers.build {
                    append(HttpHeaders.ContentType, "application/gpx+xml")
                    append(HttpHeaders.ContentDisposition, "filename=sector.gpx")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        var sectorGpx: String? = null

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)

            val gpxFile = sector.gpx
            assertNotNull(gpxFile)
            sectorGpx = gpxFile.toRelativeString(Storage.ImagesDir)
            assertTrue(gpxFile.exists())

            assertNotEquals(oldTimestamp, sector.timestamp)
        }

        get("/file/$sectorGpx").apply {
            assertSuccess { data ->
                assertNotNull(data)
                val serverHash = data.getString("hash")
                val localHash = HashUtils.getCheckSumFromStream(
                    MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
                    this::class.java.getResourceAsStream("/tracks/ulldelmoro.gpx")!!
                )
                assertEquals(localHash, serverHash)
            }
        }
    }

    @Test
    fun `test patching Sector - remove walking time`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("walkingTime", "\u0000")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertNull(sector.walkingTime)

            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }

    @Test
    fun `test patching Sector - remove point`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val oldTimestamp = ServerDatabase.instance.query { Sector[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/sector/$sectorId",
            formData = formData {
                append("point", "\u0000")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val sector = Sector[sectorId]
            assertNotNull(sector)
            assertNull(sector.point)

            assertNotEquals(oldTimestamp, sector.timestamp)
        }
    }
}
