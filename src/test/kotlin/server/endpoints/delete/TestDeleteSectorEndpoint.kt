package server.endpoints.delete

import database.EntityTypes
import database.entity.Sector
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.base.delete.SingleFileRemoval
import server.base.provide
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

    @Test
    fun `test deleting Sector with children`() = testDeleting(
        EntityTypes.SECTOR,
        listOf(SingleFileRemoval(Sector::image), SingleFileRemoval(Sector::gpx)),
        provideChildren = { sectorId ->
            EntityTypes.PATH.provide(this, provideParent = { sectorId })
        }
    )
}
