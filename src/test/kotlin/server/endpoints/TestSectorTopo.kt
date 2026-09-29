package server.endpoints

import ServerDatabase
import assertions.assertFailure
import assertions.assertSuccess
import data.TopoSamples
import org.escalaralcoiaicomtat.common.topo.Topo
import database.EntityTypes
import database.entity.Sector
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.base.StubApplicationTestBuilder
import server.base.provide
import server.error.Errors
import server.response.query.TopoResponseData
import server.response.update.UpdateResponseData
import utils.MimeTypes

class TestSectorTopo : ApplicationTestBase() {
    private fun resource(path: String): ByteArray = this::class.java.getResourceAsStream(path)!!.use { it.readBytes() }

    private suspend fun StubApplicationTestBuilder.patchSector(
        sectorId: Int,
        topo: String? = null,
        background: String? = null,
        image: String? = null
    ): HttpResponse = client.submitFormWithBinaryData(
        url = "/sector/$sectorId",
        formData = formData {
            topo?.let { append("topo", it) }
            background?.let {
                append("topoImage", resource(it), Headers.build {
                    append(HttpHeaders.ContentType, MimeTypes.JPEG)
                    append(HttpHeaders.ContentDisposition, "filename=topo.jpg")
                })
            }
            image?.let {
                append("image", resource(it), Headers.build {
                    append(HttpHeaders.ContentType, MimeTypes.JPEG)
                    append(HttpHeaders.ContentDisposition, "filename=image.jpg")
                })
            }
        }
    ) {
        header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
    }

    private fun Topo.encode() = Topo.json.encodeToString(Topo.serializer(), this)

    /** Creates a sector with one path, and returns their IDs. */
    private suspend fun StubApplicationTestBuilder.sectorWithPath(): Pair<Int, Int> {
        val sectorId = assertNotNull(EntityTypes.SECTOR.provide(this))
        val pathId = assertNotNull(DataProvider.provideSamplePath(this, sectorId))
        return sectorId to pathId
    }

    private suspend fun imageNameOf(sectorId: Int) =
        ServerDatabase.instance.query { Sector.findById(sectorId)!!.image.name }

    @Test
    fun `test patching a topo renders the sector image`() = test {
        val (sectorId, pathId) = sectorWithPath()
        val oldImage = imageNameOf(sectorId)

        patchSector(sectorId, topo = TopoSamples.sampleTopo(pathId).encode(), background = "/images/uixola.jpg")
            .assertSuccess<UpdateResponseData<Sector>>()

        ServerDatabase.instance.query {
            val sector = Sector.findById(sectorId)!!
            assertEquals(TopoSamples.sampleTopo(pathId), sector.topo)
            val background = assertNotNull(sector.topoImage)
            assertNotEquals(oldImage, sector.image.name)
            assertEquals("webp", sector.image.extension)

            val rendered = ImageIO.read(sector.image)
            val original = ImageIO.read(background)
            assertEquals(original.width, rendered.width)
            assertEquals(original.height, rendered.height)
        }
    }

    @Test
    fun `test topo without background is rejected`() = test {
        val (sectorId, pathId) = sectorWithPath()
        patchSector(sectorId, topo = TopoSamples.sampleTopo(pathId).encode()).assertFailure(Errors.MissingData)
    }

    @Test
    fun `test invalid topo is rejected`() = test {
        val (sectorId, _) = sectorWithPath()
        val invalid = TopoSamples.sampleTopo().copy(routes = listOf(Topo.Route(1, listOf("missing"))))
        patchSector(sectorId, topo = invalid.encode(), background = "/images/uixola.jpg")
            .assertFailure(Errors.InvalidData)
        patchSector(sectorId, topo = "not json", background = "/images/uixola.jpg")
            .assertFailure(Errors.InvalidData)
    }

    @Test
    fun `test image and topo together conflict`() = test {
        val (sectorId, pathId) = sectorWithPath()
        patchSector(
            sectorId,
            topo = TopoSamples.sampleTopo(pathId).encode(),
            background = "/images/uixola.jpg",
            image = "/images/desploms2.jpg"
        ).assertFailure(Errors.Conflict)
    }

    @Test
    fun `test topo endpoint returns the topo`() = test {
        val (sectorId, pathId) = sectorWithPath()
        get("/sector/$sectorId/topo").assertFailure(Errors.ObjectNotFound)

        patchSector(sectorId, topo = TopoSamples.sampleTopo(pathId).encode(), background = "/images/uixola.jpg")
            .assertSuccess<UpdateResponseData<Sector>>()

        val background = ServerDatabase.instance.query { Sector.findById(sectorId)!!.topoImage!!.nameWithoutExtension }
        get("/sector/$sectorId/topo").assertSuccess<TopoResponseData> { data ->
            assertNotNull(data)
            assertEquals(TopoSamples.sampleTopo(pathId), data.topo)
            assertEquals(background, data.topoImage)
        }
    }

    @Test
    fun `test changing a route re-renders the topo`() = test {
        val (sectorId, pathId) = sectorWithPath()
        patchSector(sectorId, topo = TopoSamples.sampleTopo(pathId).encode(), background = "/images/uixola.jpg")
            .assertSuccess<UpdateResponseData<Sector>>()
        val rendered = imageNameOf(sectorId)

        client.submitFormWithBinaryData(
            url = "/path/$pathId",
            formData = formData { append("grade", "G7A") }
        ) {
            header(HttpHeaders.Authorization, "Bearer $AUTH_TOKEN")
        }.assertSuccess<UpdateResponseData<database.entity.Path>>()

        assertNotEquals(rendered, imageNameOf(sectorId))
    }

    @Test
    fun `test uploading an image removes the topo`() = test {
        val (sectorId, pathId) = sectorWithPath()
        patchSector(sectorId, topo = TopoSamples.sampleTopo(pathId).encode(), background = "/images/uixola.jpg")
            .assertSuccess<UpdateResponseData<Sector>>()

        patchSector(sectorId, image = "/images/desploms2.jpg").assertSuccess<UpdateResponseData<Sector>>()

        ServerDatabase.instance.query {
            val sector = Sector.findById(sectorId)!!
            assertNull(sector.topo)
            assertNull(sector.topoImage)
            assertEquals("jpg", sector.image.extension)
        }
    }

    @Test
    fun `test removing the topo keeps the rendered image`() = test {
        val (sectorId, pathId) = sectorWithPath()
        patchSector(sectorId, topo = TopoSamples.sampleTopo(pathId).encode(), background = "/images/uixola.jpg")
            .assertSuccess<UpdateResponseData<Sector>>()
        val rendered = imageNameOf(sectorId)

        patchSector(sectorId, topo = "\u0000").assertSuccess<UpdateResponseData<Sector>>()

        ServerDatabase.instance.query {
            val sector = Sector.findById(sectorId)!!
            assertNull(sector.topo)
            assertNull(sector.topoImage)
            assertEquals(rendered, sector.image.name)
        }
    }
}
