package server.endpoints.block

import ServerDatabase
import assertions.assertSuccess
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.EntityTypes
import database.entity.Blocking
import database.entity.info.LastUpdate
import distribution.Notifier
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import java.time.LocalDateTime
import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import server.DataProvider
import server.base.ApplicationTestBase
import server.request.AddBlockRequest
import server.response.update.UpdateResponseData

class TestPatchBlockEndpoint: ApplicationTestBase() {
    private fun patch(
        request: AddBlockRequest,
        assert: (element: Blocking) -> Unit
    ) = test {
        val areaId = with(DataProvider) { provideSampleArea() }
        val zoneId = with(DataProvider) { provideSampleZone(areaId) }
        val sectorId = with(DataProvider) { provideSampleSector(zoneId) }
        val pathId = with(DataProvider) { provideSamplePath(sectorId) }

        var blockId: Int? = null

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        post("/block/$pathId") {
            setBody(
                AddBlockRequest(BlockingTypes.BUILD)
            )
        }.apply {
            assertSuccess<UpdateResponseData<Blocking>>(HttpStatusCode.Created) {
                val element = it?.element
                assertNotNull(element)
                blockId = element.id.value
            }

            ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }
        }
        assertNotNull(blockId)

        patch("/block/$blockId") {
            setBody(request)
        }.apply {
            assertSuccess<UpdateResponseData<Blocking>>(HttpStatusCode.OK) {
                val element = it?.element
                assertNotNull(element)
                assert(element)
            }
        }

        assertNotificationSent(Notifier.TOPIC_UPDATED, EntityTypes.BLOCKING, blockId!!)
    }

    @Test
    fun `test block patching - type`() = patch(
        AddBlockRequest(type = BlockingTypes.BIRD)
    ) {
        assertEquals(BlockingTypes.BIRD, it.type)
    }

    @Test
    fun `test block patching - recurrence`() {
        val recurrence = BlockingRecurrenceYearly(1U, Month.MARCH, 1U, Month.JULY)

        patch(
            AddBlockRequest(recurrence = recurrence)
        ) {
            assertEquals(recurrence, it.recurrence)
        }
    }

    @Test
    fun `test block patching - endDate`() {
        val endDate = LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0)

        patch(
            AddBlockRequest(endDate = endDate)
        ) {
            assertEquals(endDate, it.endDate)
        }
    }
}
