package server.endpoints.patch

import ServerDatabase
import assertions.assertSuccess
import data.Builder
import data.Ending
import data.Grade
import data.PitchInfo
import database.EntityTypes
import database.entity.Path
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.base.patch.PropertyValuePair
import server.base.testPatching

class TestPatchPathEndpoint : ApplicationTestBase() {

    private fun <T> removeProperty(propertyName: String, propertyValue: (Path) -> T) = test {
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)

        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        assertNotNull(zoneId)

        val sectorId = with(DataProvider) { provideSampleSector(zoneId) }
        assertNotNull(sectorId)

        val pathId = with(DataProvider) { provideSamplePath(sectorId) }
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
        testPatching(EntityTypes.PATH, "displayName", "New displayName") { it.displayName }

    @Test
    fun `test patching Path - update sketchId`() =
        testPatching(EntityTypes.PATH, "sketchId", 10U) { it.sketchId }

    @Test
    fun `test patching Path - update height`() =
        testPatching(EntityTypes.PATH, "height", 100U) { it.height }

    @Test
    fun `test patching Path - update grade`() =
        testPatching(EntityTypes.PATH, "grade", Grade.G7C) { it.grade }

    @Test
    fun `test patching Path - update ending`() =
        testPatching(EntityTypes.PATH, "ending", Ending.NONE) { it.ending }

    @Test
    fun `test patching Path - update pitches`() =
        testPatching(EntityTypes.PATH, "pitches", listOf(PitchInfo(1U))) { it.pitches }

    @Test
    fun `test patching Path - update pitches and gradle`() =
        testPatching(
            EntityTypes.PATH,
            listOf(
                PropertyValuePair("pitches", listOf(PitchInfo(1U))) { it.pitches },
                PropertyValuePair("grade", Grade.G7C) { it.grade }
            )
        )

    @Test
    fun `test patching Path - update stringCount`() =
        testPatching(EntityTypes.PATH, "stringCount", 456U) { it.stringCount }

    @Test
    fun `test patching Path - update paraboltCount`() =
        testPatching(EntityTypes.PATH, "paraboltCount", 456U) { it.paraboltCount }

    @Test
    fun `test patching Path - update burilCount`() =
        testPatching(EntityTypes.PATH, "burilCount", 456U) { it.burilCount }

    @Test
    fun `test patching Path - update pitonCount`() =
        testPatching(EntityTypes.PATH, "pitonCount", 456U) { it.pitonCount }

    @Test
    fun `test patching Path - update spitCount`() =
        testPatching(EntityTypes.PATH, "spitCount", 456U) { it.spitCount }

    @Test
    fun `test patching Path - update tensorCount`() =
        testPatching(EntityTypes.PATH, "tensorCount", 456U) { it.tensorCount }

    @Test
    fun `test patching Path - update crackerRequired`() =
        testPatching(EntityTypes.PATH, "crackerRequired", false) { it.crackerRequired }

    @Test
    fun `test patching Path - update friendRequired`() =
        testPatching(EntityTypes.PATH, "friendRequired", true) { it.friendRequired }

    @Test
    fun `test patching Path - update lanyardRequired`() =
        testPatching(EntityTypes.PATH, "lanyardRequired", false) { it.lanyardRequired }

    @Test
    fun `test patching Path - update nailRequired`() =
        testPatching(EntityTypes.PATH, "nailRequired", false) { it.nailRequired }

    @Test
    fun `test patching Path - update pitonRequired`() =
        testPatching(EntityTypes.PATH, "pitonRequired", true) { it.pitonRequired }

    @Test
    fun `test patching Path - update stapesRequired`() =
        testPatching(EntityTypes.PATH, "stapesRequired", false) { it.stapesRequired }

    @Test
    fun `test patching Path - update showDescription`() =
        testPatching(EntityTypes.PATH, "showDescription", false) { it.showDescription }

    @Test
    fun `test patching Path - update description`() =
        testPatching(EntityTypes.PATH, "description", "new description") { it.description }

    @Test
    fun `test patching Path - update builder`() =
        testPatching(EntityTypes.PATH, "builder", Builder("new name", "new date")) { it.builder }

    @Test
    fun `test patching Path - update reBuilder`() =
        testPatching(EntityTypes.PATH, "reBuilder", listOf(Builder("new name", "new date"))) { it.reBuilder }

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
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)

        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        assertNotNull(zoneId)

        val sectorId = with(DataProvider) { provideSampleSector(zoneId) }
        assertNotNull(sectorId)

        val pathId = with(DataProvider) {
            provideSamplePath(
                sectorId,
                images = listOf("/images/uixola.jpg", "/images/uixola.jpg")
            )
        }
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
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)

        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        assertNotNull(zoneId)

        val sectorId = with(DataProvider) { provideSampleSector(zoneId) }
        assertNotNull(sectorId)

        val pathId = with(DataProvider) { provideSamplePath(sectorId) }
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
        val areaId = with(DataProvider) { provideSampleArea() }
        assertNotNull(areaId)

        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        assertNotNull(zoneId)

        val sectorId = with(DataProvider) { provideSampleSector(zoneId) }
        assertNotNull(sectorId)

        val pathId = with(DataProvider) { provideSamplePath(sectorId, images = listOf("/images/uixola.jpg")) }
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
