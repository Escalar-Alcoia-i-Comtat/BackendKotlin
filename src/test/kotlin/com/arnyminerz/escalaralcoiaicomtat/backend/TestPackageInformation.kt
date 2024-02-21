package com.arnyminerz.escalaralcoiaicomtat.backend

import com.arnyminerz.escalaralcoiaicomtat.backend.system.Package
import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow

class TestPackageInformation {
    @Test
    fun `test version`() {
        assertDoesNotThrow {
            Package.getVersion()
        }
    }
}
