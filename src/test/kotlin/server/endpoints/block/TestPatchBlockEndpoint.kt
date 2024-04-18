package server.endpoints.block

import ServerDatabase
import assertions.assertSuccess
import data.BlockingRecurrenceYearly
import data.BlockingTypes
import database.EntityTypes
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
import org.json.JSONObject
import server.DataProvider
import server.base.ApplicationTestBase
import utils.getJSONObjectOrNull
import utils.jsonOf

class TestPatchBlockEndpoint: ApplicationTestBase() {
    private fun patch(parameterName: String, parameterValue: Any, assert: (element: JSONObject) -> Unit) = test {
        val areaId = DataProvider.provideSampleArea()
        val zoneId = DataProvider.provideSampleZone(areaId)
        val sectorId = DataProvider.provideSampleSector(zoneId)
        val pathId = DataProvider.provideSamplePath(sectorId)

        var blockId: Int? = null

        val lastUpdate = ServerDatabase.instance.query { LastUpdate.get() }

        post("/block/$pathId") {
            setBody(
                jsonOf(
                    "type" to BlockingTypes.BUILD
                ).toString()
            )
        }.apply {
            assertSuccess(HttpStatusCode.Created) {
                val element = it?.getJSONObjectOrNull("element")
                assertNotNull(element)
                blockId = element.getInt("id")
            }

            ServerDatabase.instance.query { assertNotEquals(LastUpdate.get(), lastUpdate) }
        }
        assertNotNull(blockId)

        patch("/block/$blockId") {
            setBody(
                jsonOf(parameterName to parameterValue).toString()
            )
        }.apply {
            assertSuccess {
                val element = it?.getJSONObjectOrNull("element")
                assertNotNull(element)
                assert(element)
            }
        }

        assertNotificationSent(Notifier.TOPIC_UPDATED, EntityTypes.BLOCKING, blockId!!)
    }

    @Test
    fun `test block patching - type`() = patch("type", BlockingTypes.BIRD) {
        assertEquals(BlockingTypes.BIRD, it.getEnum(BlockingTypes::class.java, "type"))
    }

    @Test
    fun `test block patching - recurrence`() {
        val recurrence = BlockingRecurrenceYearly(1U, Month.MARCH, 1U, Month.JULY)

        patch("recurrence", recurrence) {
            val actual = BlockingRecurrenceYearly.fromJson(it.getJSONObject("recurrence"))

            assertEquals(recurrence, actual)
        }
    }

    @Test
    fun `test block patching - endDate`() {
        val endDate = LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0)

        patch("end_date", endDate.toString()) {
            assertEquals(endDate.toString(), it.getString("end_date"))
        }
    }
}
