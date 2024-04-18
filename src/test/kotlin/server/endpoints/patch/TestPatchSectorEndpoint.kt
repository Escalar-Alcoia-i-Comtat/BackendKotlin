package server.endpoints.patch

import data.LatLng
import database.EntityTypes
import database.entity.Sector
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.base.testPatching
import server.base.testPatchingFile
import storage.Storage
import utils.FileExtensions
import utils.MimeTypes

class TestPatchSectorEndpoint : ApplicationTestBase() {
    @Test
    fun `test patching Sector - update display name`() = testPatching(
        EntityTypes.SECTOR,
        "displayName",
        "New Display Name"
    ) { it.displayName }

    @Test
    fun `test patching Sector - update point`() = testPatching(
        EntityTypes.SECTOR,
        "point",
        LatLng(0.456, 0.789)
    ) { it.point }

    @Test
    fun `test patching Sector - update kids apt`() = testPatching(
        EntityTypes.SECTOR,
        "kidsApt",
        false
    ) { it.kidsApt }

    @Test
    fun `test patching Sector - update sun time`() = testPatching(
        EntityTypes.SECTOR,
        "sunTime",
        Sector.SunTime.Morning
    ) { it.sunTime }

    @Test
    fun `test patching Sector - update walking time`() = testPatching(
        EntityTypes.SECTOR,
        "walkingTime",
        9510U
    ) { it.walkingTime }

    @Test
    fun `test patching Sector - update weight`() = testPatching(
        EntityTypes.SECTOR,
        "weight",
        "0123"
    ) { it.weight }

    @Test
    fun `test patching Sector - update image`() = testPatchingFile(
        EntityTypes.SECTOR,
        "image",
        MimeTypes.JPEG,
        FileExtensions.JPEG,
        "/images/desploms2.jpg",
        Storage.ImagesDir
    ) { it.image }

    @Test
    fun `test patching Sector - update gpx`() = testPatchingFile(
        EntityTypes.SECTOR,
        "gpx",
        MimeTypes.GPX,
        FileExtensions.GPX,
        "/tracks/ulldelmoro.gpx",
        Storage.TracksDir
    ) { it.gpx }

    @Test
    fun `test patching Sector - remove gpx`() = testPatchingFile(
        EntityTypes.SECTOR,
        "gpx",
        MimeTypes.GPX,
        FileExtensions.GPX,
        null,
        Storage.TracksDir
    ) { it.gpx }

    @Test
    fun `test patching Sector - remove walking time`() = testPatching(
        EntityTypes.SECTOR,
        "walkingTime",
        null
    ) { it.walkingTime }

    @Test
    fun `test patching Sector - remove point`() = testPatching(
        EntityTypes.SECTOR,
        "point",
        null
    ) { it.point }
}
