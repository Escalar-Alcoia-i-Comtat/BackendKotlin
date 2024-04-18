package server.endpoints.patch

import database.EntityTypes
import kotlin.test.Test
import server.base.ApplicationTestBase
import server.base.testPatching
import server.base.testPatchingFile
import storage.Storage
import utils.FileExtensions
import utils.MimeTypes

class TestPatchAreaEndpoint : ApplicationTestBase() {
    @Test
    fun `test patching Area - update display name`() = testPatching(
        EntityTypes.AREA,
        "displayName",
        "New Display Name"
    ) { it.displayName }

    @Test
    fun `test patching Area - update web url`() = testPatching(
        EntityTypes.AREA,
        "webUrl",
        "https://example.com/new"
    ) { it.webUrl.toString() }

    @Test
    fun `test patching Area - update image`() = testPatchingFile(
        EntityTypes.AREA,
        "image",
        MimeTypes.JPEG,
        FileExtensions.JPEG,
        "/images/cocentaina.jpg",
        Storage.ImagesDir
    ) { it.image }
}
