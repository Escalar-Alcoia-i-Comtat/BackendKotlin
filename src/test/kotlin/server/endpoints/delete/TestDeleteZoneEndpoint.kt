package server.endpoints.delete

import database.EntityTypes
import database.entity.Zone
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.base.delete.SingleFileRemoval
import server.base.testDeleting

class TestDeleteZoneEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Zone`() = testDeleting(
        EntityTypes.ZONE,
        listOf(SingleFileRemoval(Zone::image), SingleFileRemoval(Zone::kmz))
    )

    @Test
    fun `test deleting non existing Zone`() = testDeleting(EntityTypes.ZONE)
}
