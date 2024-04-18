package server.endpoints.patch

import database.EntityTypes
import kotlin.test.Test
import server.DataProvider
import server.base.ApplicationTestBase
import server.base.testPatching
import server.base.testPatchingFile
import storage.Storage
import utils.FileExtensions
import utils.MimeTypes

class TestPatchZoneEndpoint: ApplicationTestBase() {
    @Test
    fun `test patching Zone - update display name`() = testPatching(
        EntityTypes.ZONE,
        "displayName",
        "New Display Name"
    ) { it.displayName }

    @Test
    fun `test patching Zone - update web url`() = testPatching(
        EntityTypes.ZONE,
        "webUrl",
        "https://example.com/new"
    ) { it.webUrl.toString() }

    @Test
    fun `test patching Zone - update point`() = testPatching(
        EntityTypes.ZONE,
        "point",
        DataProvider.SampleZone
            .point
            .copy(latitude = 0.98765, longitude = 0.43210)
    ) { it.point }

    @Test
    fun `test patching Zone - update points`() = testPatching(
        EntityTypes.ZONE,
        "points",
        null
    ) { it.points }

    @Test
    fun `test patching Zone - remove point`() = testPatching(
        EntityTypes.ZONE,
        "point",
        DataProvider.SampleZone
            .point
            .copy(latitude = 0.98765, longitude = 0.43210)
    ) { it.point }

    @Test
    fun `test patching Zone - update image`() = testPatchingFile(
        EntityTypes.ZONE,
        "image",
        MimeTypes.JPEG,
        FileExtensions.JPEG,
        "/images/cocentaina.jpg",
        Storage.ImagesDir
    ) { it.image }

    @Test
    fun `test patching Zone - update track`() = testPatchingFile(
        EntityTypes.ZONE,
        "kmz",
        MimeTypes.KMZ,
        FileExtensions.KMZ,
        "/tracks/balconet.kmz",
        Storage.TracksDir
    ) { it.kmz }
}
