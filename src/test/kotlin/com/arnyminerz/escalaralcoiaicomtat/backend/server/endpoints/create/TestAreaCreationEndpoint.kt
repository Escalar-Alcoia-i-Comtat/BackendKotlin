package com.arnyminerz.escalaralcoiaicomtat.backend.server.endpoints.create

import com.arnyminerz.escalaralcoiaicomtat.backend.ServerDatabase
import com.arnyminerz.escalaralcoiaicomtat.backend.assertions.assertFailure
import com.arnyminerz.escalaralcoiaicomtat.backend.database.entity.Area
import com.arnyminerz.escalaralcoiaicomtat.backend.server.DataProvider
import com.arnyminerz.escalaralcoiaicomtat.backend.server.base.ApplicationTestBase
import com.arnyminerz.escalaralcoiaicomtat.backend.server.error.Errors
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestAreaCreationEndpoint: ApplicationTestBase() {
    @Test
    fun `test area creation`() = test {
        val areaId: Int? = DataProvider.provideSampleArea()
        assertNotNull(areaId)

        ServerDatabase.instance.query {
            val area = Area[areaId]
            assertNotNull(area)
            assertEquals(DataProvider.SampleArea.displayName, area.displayName)
            assertEquals(URL(DataProvider.SampleArea.webUrl), area.webUrl)

            val imageFile = area.image
            assertTrue(imageFile.exists())
        }
    }

    @Test
    fun `test area creation - missing arguments`() = test {
        DataProvider.provideSampleArea(skipDisplayName = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSampleArea(skipWebUrl = true) {
            assertFailure(Errors.MissingData)
            null
        }
        DataProvider.provideSampleArea(skipImage = true) {
            assertFailure(Errors.MissingData)
            null
        }
    }
}
