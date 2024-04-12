package server.endpoints.patch

import ServerDatabase
import assertions.assertSuccess
import data.Builder
import data.Ending
import data.PitchInfo
import data.SportsGrade
import database.entity.Path
import database.entity.info.LastUpdate
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.json.JSONArray
import server.DataProvider
import server.base.ApplicationTestBase
import utils.serialization.JsonSerializable
import utils.toJson

class TestPatchPathEndpoint : ApplicationTestBase() {
    data class PropertyValuePair<T>(val propertyName: String, val newValue: T, val propertyValue: (Path) -> T)

    private fun <T> patchProperty(propertyName: String, newValue: T, propertyValue: (Path) -> T) =
        patchProperty(PropertyValuePair(propertyName, newValue, propertyValue))

    private fun <T> patchProperty(vararg pairs: PropertyValuePair<T>) = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }
        val oldTimestamp = ServerDatabase.instance.query { Path[sectorId].timestamp }

        client.submitFormWithBinaryData(
            url = "/path/$sectorId",
            formData = formData {
                for ((propertyName, newValue) in pairs) {
                    when (newValue) {
                        is JsonSerializable -> append(propertyName, newValue.toJson().toString())
                        is Number -> append(propertyName, newValue)
                        is Iterable<*> -> if (newValue.firstOrNull() is JsonSerializable)
                            @Suppress("UNCHECKED_CAST")
                            append(propertyName, (newValue as Iterable<JsonSerializable>).toJson().toString())
                        else
                            append(propertyName, JSONArray().toString())

                        else -> append(propertyName, newValue.toString())
                    }
                }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)
            for ((_, newValue, propertyValue) in pairs) {
                if (newValue is Iterable<*>)
                    assertContentEquals(newValue, path.let(propertyValue) as Iterable<Any?>)
                else
                    assertEquals(newValue, path.let(propertyValue))
            }
            assertNotEquals(oldTimestamp, path.timestamp)
        }
    }

    private fun <T> removeProperty(propertyName: String, propertyValue: (Path) -> T) = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        client.submitFormWithBinaryData(
            url = "/path/$sectorId",
            formData = formData {
                append(propertyName, "\u0000")
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)
            assertNull(path.let(propertyValue))
        }
    }

    @Test
    fun `test patching Path - update display name`() =
        patchProperty("displayName", "New displayName") { it.displayName }

    @Test
    fun `test patching Path - update sketchId`() =
        patchProperty("sketchId", 10U) { it.sketchId }

    @Test
    fun `test patching Path - update height`() =
        patchProperty("height", 100U) { it.height }

    @Test
    fun `test patching Path - update grade`() =
        patchProperty("grade", SportsGrade.G7C) { it.grade }

    @Test
    fun `test patching Path - update ending`() =
        patchProperty("ending", Ending.NONE) { it.ending }

    @Test
    fun `test patching Path - update pitches`() =
        patchProperty("pitches", listOf(PitchInfo(1U))) { it.pitches }

    @Test
    fun `test patching Path - update pitches and gradle`() =
        patchProperty(
            PropertyValuePair("pitches", listOf(PitchInfo(1U))) { it.pitches },
            PropertyValuePair("grade", SportsGrade.G7C) { it.grade }
        )

    @Test
    fun `test patching Path - update stringCount`() =
        patchProperty("stringCount", 456U) { it.stringCount }

    @Test
    fun `test patching Path - update paraboltCount`() =
        patchProperty("paraboltCount", 456U) { it.paraboltCount }

    @Test
    fun `test patching Path - update burilCount`() =
        patchProperty("burilCount", 456U) { it.burilCount }

    @Test
    fun `test patching Path - update pitonCount`() =
        patchProperty("pitonCount", 456U) { it.pitonCount }

    @Test
    fun `test patching Path - update spitCount`() =
        patchProperty("spitCount", 456U) { it.spitCount }

    @Test
    fun `test patching Path - update tensorCount`() =
        patchProperty("tensorCount", 456U) { it.tensorCount }

    @Test
    fun `test patching Path - update crackerRequired`() =
        patchProperty("crackerRequired", false) { it.crackerRequired }

    @Test
    fun `test patching Path - update friendRequired`() =
        patchProperty("friendRequired", true) { it.friendRequired }

    @Test
    fun `test patching Path - update lanyardRequired`() =
        patchProperty("lanyardRequired", false) { it.lanyardRequired }

    @Test
    fun `test patching Path - update nailRequired`() =
        patchProperty("nailRequired", false) { it.nailRequired }

    @Test
    fun `test patching Path - update pitonRequired`() =
        patchProperty("pitonRequired", true) { it.pitonRequired }

    @Test
    fun `test patching Path - update stapesRequired`() =
        patchProperty("stapesRequired", false) { it.stapesRequired }

    @Test
    fun `test patching Path - update showDescription`() =
        patchProperty("showDescription", false) { it.showDescription }

    @Test
    fun `test patching Path - update description`() =
        patchProperty("description", "new description") { it.description }

    @Test
    fun `test patching Path - update builder`() =
        patchProperty("builder", Builder("new name", "new date")) { it.builder }

    @Test
    fun `test patching Path - update reBuilder`() =
        patchProperty("reBuilder", listOf(Builder("new name", "new date"))) { it.reBuilder }

    @Test
    fun `test patching Path - remove height`() = removeProperty("height") { it.height }

    @Test
    fun `test patching Path - remove grade`() = removeProperty("grade") { it.grade }

    @Test
    fun `test patching Path - remove ending`() = removeProperty("ending") { it.ending }

    @Test
    fun `test patching Path - remove pitches`() = removeProperty("pitches") { it.pitches }

    @Test
    fun `test patching Path - remove stringCount`() = removeProperty("stringCount") { it.stringCount }

    @Test
    fun `test patching Path - remove paraboltCount`() = removeProperty("paraboltCount") { it.paraboltCount }

    @Test
    fun `test patching Path - remove burilCount`() = removeProperty("burilCount") { it.burilCount }

    @Test
    fun `test patching Path - remove pitonCount`() = removeProperty("pitonCount") { it.pitonCount }

    @Test
    fun `test patching Path - remove spitCount`() = removeProperty("spitCount") { it.spitCount }

    @Test
    fun `test patching Path - remove tensorCount`() = removeProperty("tensorCount") { it.tensorCount }

    @Test
    fun `test patching Path - remove description`() = removeProperty("description") { it.description }

    @Test
    fun `test patching Path - remove builder`() = removeProperty("builder") { it.builder }

    @Test
    fun `test patching Path - remove reBuilder`() = removeProperty("reBuilder") { it.reBuilder }

    @Test
    fun `test patching Path - remove image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(
            sectorId,
            images = listOf("/images/uixola.jpg", "/images/uixola.jpg")
        )
        assertNotNull(pathId)

        val newPath = ServerDatabase.instance.query { Path[pathId] }

        client.submitFormWithBinaryData(
            url = "/path/$sectorId",
            formData = formData {
                append("removeImages", newPath.images!!.first().name)
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)

            val images = path.images
            assertNotNull(images)
            assertEquals(1, images.size) // one image has been removed
        }
    }

    @Test
    fun `test patching Path - add image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId)
        assertNotNull(pathId)

        val image = this::class.java.getResourceAsStream("/images/uixola.jpg")!!.use { it.readBytes() }

        client.submitFormWithBinaryData(
            url = "/path/$sectorId",
            formData = formData {
                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=path.jpg")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)

            val images = path.images
            assertNotNull(images)
            assertEquals(1, images.size)
        }
    }

    @Test
    fun `test patching Path - add and remove image`() = test {
        val areaId = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        val zoneId = DataProvider.provideSampleZone(areaId)
        assertNotNull(zoneId)

        val sectorId = DataProvider.provideSampleSector(zoneId)
        assertNotNull(sectorId)

        val pathId = DataProvider.provideSamplePath(sectorId, images = listOf("/images/uixola.jpg"))
        assertNotNull(pathId)

        val newPath = ServerDatabase.instance.query { Path[pathId] }

        val image = this::class.java.getResourceAsStream("/images/uixola.jpg")!!.use { it.readBytes() }

        client.submitFormWithBinaryData(
            url = "/path/$sectorId",
            formData = formData {
                append("removeImages", newPath.images!!.first().name)

                append("image", image, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=path.jpg")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.apply {
            assertSuccess()
        }

        ServerDatabase.instance.query {
            val path = Path[pathId]
            assertNotNull(path)

            val images = path.images
            assertNotNull(images)
            assertEquals(1, images.size)
        }
    }
}
