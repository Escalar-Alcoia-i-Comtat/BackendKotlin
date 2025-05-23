package server.endpoints.delete

import database.EntityTypes
import database.entity.Area
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.base.delete.SingleFileRemoval
import server.base.provide
import server.base.testDeleting
import server.base.testDeletingNotFound

class TestDeleteAreaEndpoint: ApplicationTestBase() {
    @Test
    fun `test deleting Area`() = testDeleting(
        EntityTypes.AREA,
        listOf(SingleFileRemoval(Area::image))
    )

    @Test
    fun `test deleting non existing Area`() = testDeletingNotFound(EntityTypes.AREA)

    @Test
    fun `test deleting Area with children`() = testDeleting(
        EntityTypes.AREA,
        listOf(SingleFileRemoval(Area::image)),
        provideChildren = { areaId ->
            EntityTypes.ZONE.provide(this, provideParent = { areaId })
        }
    )
}
