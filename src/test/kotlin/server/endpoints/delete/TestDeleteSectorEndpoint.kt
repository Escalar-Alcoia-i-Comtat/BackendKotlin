package server.endpoints.delete

import database.EntityTypes
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.base.delete.SingleFileRemoval
import server.base.testDeleting
import server.base.testDeletingNotFound

class TestDeleteSectorEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Sector`() = testDeleting(
        EntityTypes.SECTOR,
        listOf(SingleFileRemoval { it.image }, SingleFileRemoval { it.gpx })
    )

    @Test
    fun `test deleting non existing Sector`() = testDeletingNotFound(EntityTypes.SECTOR)
}
